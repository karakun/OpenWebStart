package com.openwebstart.jvm;

import com.openwebstart.func.Result;
import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.http.HttpGetRequest;
import com.openwebstart.http.HttpResponse;
import com.openwebstart.jvm.json.CacheStore;
import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.listener.Registration;
import com.openwebstart.jvm.listener.RuntimeAddedListener;
import com.openwebstart.jvm.listener.RuntimeRemovedListener;
import com.openwebstart.jvm.listener.RuntimeUpdateListener;
import com.openwebstart.jvm.localfinder.RuntimeFinder;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.jvm.util.FolderFactory;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import com.openwebstart.util.ZipUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

public final class LocalRuntimeManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRuntimeManager.class);

    private static final LocalRuntimeManager INSTANCE = new LocalRuntimeManager();

    private final List<LocalJavaRuntime> runtimes = new CopyOnWriteArrayList<>();

    private final List<RuntimeRemovedListener> removedListeners = new CopyOnWriteArrayList<>();
    private final List<RuntimeAddedListener> addedListeners = new CopyOnWriteArrayList<>();
    private final List<RuntimeUpdateListener> updatedListeners = new CopyOnWriteArrayList<>();

    private final Lock jsonStoreLock = new ReentrantLock();

    private LocalRuntimeManager() {
    }

    public List<LocalJavaRuntime> getAll() {
        return Collections.unmodifiableList(runtimes);
    }

    public Registration addRuntimeAddedListener(final RuntimeAddedListener listener) {
        addedListeners.add(listener);
        return () -> addedListeners.remove(listener);
    }

    public Registration addRuntimeRemovedListener(final RuntimeRemovedListener listener) {
        removedListeners.add(listener);
        return () -> removedListeners.remove(listener);
    }

    public Registration addRuntimeUpdatedListener(final RuntimeUpdateListener listener) {
        updatedListeners.add(listener);
        return () -> updatedListeners.remove(listener);
    }

    private void saveRuntimes() throws Exception {
        jsonStoreLock.lock();
        try {
            LOG.debug("Saving runtime cache to filesystem");
            final File cachePath = cacheBaseDir();
            if (!cachePath.exists()) {
                final boolean dirCreated = cachePath.mkdirs();
                if (!dirCreated) {
                    throw new IOException("Can not create cache dir '" + cachePath + "'");
                }
            }
            final File jsonFile = new File(cachePath, RuntimeManagerConstants.JSON_STORE_FILENAME);
            if (jsonFile.exists()) {
                jsonFile.delete();
            }
            final CacheStore cacheStore = new CacheStore(runtimes);
            final String jsonString = JsonHandler.getInstance().toJson(cacheStore);
            FileUtils.saveFileUtf8(jsonString, jsonFile);
        } finally {
            jsonStoreLock.unlock();
        }
    }

    public void loadRuntimes() {
        LOG.debug("Loading runtime cache from filesystem");
        jsonStoreLock.lock();
        final File jsonFile = new File(cacheBaseDir(), RuntimeManagerConstants.JSON_STORE_FILENAME);
        try {
            if (jsonFile.exists()) {
                final String content = FileUtils.loadFileAsUtf8String(jsonFile);
                final CacheStore cacheStore = JsonHandler.getInstance().fromJson(content, CacheStore.class);
                clear();
                cacheStore.getRuntimes().forEach(r -> add(r));
            } else {
                clear();
            }
        } catch (IOException e) {
            LOG.error("Could not load file: {}", jsonFile);
            throw new RuntimeException(e);
        } finally {
            jsonStoreLock.unlock();
        }
    }

    private void clear() {
        LOG.debug("Clearing runtime cache");
        runtimes.forEach(r -> {
            if (runtimes.contains(r)) {
                final boolean removed = runtimes.remove(r);

                if (removed) {
                    removedListeners.forEach(l -> l.onRuntimeRemoved(r));
                }
            }
        });
        try {
            saveRuntimes();
        } catch (final Exception e) {
            throw new RuntimeException("Error while saving JVM cache.", e);
        }
    }

    public void replace(final LocalJavaRuntime oldRuntime, final LocalJavaRuntime newRuntime) {
        LOG.debug("Replacing runtime definition with new one");

        Assert.requireNonNull(oldRuntime, "oldRuntime");
        Assert.requireNonNull(newRuntime, "newRuntime");

        if (!Objects.equals(oldRuntime.getJavaHome(), newRuntime.getJavaHome())) {
            throw new IllegalArgumentException("Can only replace a runtime with same JAVA_HOME");
        }

        if (!Objects.equals(oldRuntime.isManaged(), newRuntime.isManaged())) {
            throw new IllegalArgumentException("Can not change managed state of runtime");
        }

        final int index = runtimes.indexOf(oldRuntime);
        if (index < 0) {
            throw new IllegalArgumentException("Item is not in collection!");
        }
        runtimes.remove(index);
        runtimes.add(index, newRuntime);
        updatedListeners.forEach(l -> l.onRuntimeUpdated(oldRuntime, newRuntime));
        try {
            saveRuntimes();
        } catch (final Exception e) {
            throw new RuntimeException("Error while saving JVM cache.", e);
        }
    }

    public void add(final LocalJavaRuntime localJavaRuntime) {
        LOG.debug("Adding runtime definition");

        Assert.requireNonNull(localJavaRuntime, "localJavaRuntime");

        //final VersionString supportedRange = RuntimeManagerConfig.getInstance().getSupportedVersionRange();
        //if(!Optional.ofNullable(supportedRange).map(v -> v.contains(localJavaRuntime.getVersion())).orElse(true)) {
        //    throw new IllegalStateException("Runtime version '" + localJavaRuntime.getVersion() + "' do not match to supported version range '" + supportedRange + "'");
        //}

        final Path runtimePath = localJavaRuntime.getJavaHome();
        if (!runtimePath.toFile().exists()) {
            throw new IllegalArgumentException("Can not add runtime with nonexisting JAVAHOME=" + runtimePath);
        }

        if (!runtimes.contains(localJavaRuntime)) {
            runtimes.add(localJavaRuntime);
            addedListeners.forEach(l -> l.onRuntimeAdded(localJavaRuntime));
            try {
                saveRuntimes();
            } catch (final Exception e) {
                throw new RuntimeException("Error while saving JVM cache.", e);
            }
        }
    }

    public void delete(final LocalJavaRuntime localJavaRuntime) {
        Assert.requireNonNull(localJavaRuntime, "localJavaRuntime");

        LOG.debug("Deleting runtime");

        if (!localJavaRuntime.isManaged()) {
            throw new IllegalArgumentException("Can not delete runtime that is not managed");
        }

        if (runtimes.contains(localJavaRuntime)) {
            final boolean removed = runtimes.remove(localJavaRuntime);

            if (removed && localJavaRuntime.isManaged()) {
                final Path runtimeDir = localJavaRuntime.getJavaHome();
                try {
                    FileUtils.recursiveDelete(runtimeDir.toFile(), cacheBaseDir());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (removed) {
                removedListeners.forEach(l -> l.onRuntimeRemoved(localJavaRuntime));
            }
            try {
                saveRuntimes();
            } catch (final Exception e) {
                throw new RuntimeException("Error while saving JVM cache.", e);
            }
        }
    }

    public void remove(final LocalJavaRuntime localJavaRuntime) {
        Assert.requireNonNull(localJavaRuntime, "localJavaRuntime");

        LOG.debug("Removing runtime definition");

        if (localJavaRuntime.isManaged()) {
            throw new IllegalArgumentException("Can not remove runtime that is managed");
        }

        if (runtimes.contains(localJavaRuntime)) {
            final boolean removed = runtimes.remove(localJavaRuntime);

            if (removed) {
                removedListeners.forEach(l -> l.onRuntimeRemoved(localJavaRuntime));
            }
            try {
                saveRuntimes();
            } catch (final Exception e) {
                throw new RuntimeException("Error while saving JVM cache.", e);
            }
        }
    }

    public static LocalRuntimeManager getInstance() {
        return INSTANCE;
    }

    public List<Result<LocalJavaRuntime>> findAndAddLocalRuntimes() {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();

        final List<Result<LocalJavaRuntime>> foundRuntimes = new ArrayList<>();
        ServiceLoader.load(RuntimeFinder.class).iterator().forEachRemaining(f -> {
            if (f.getSupportedOperationSystems().contains(currentOs)) {
                try {
                    foundRuntimes.addAll(f.findLocalRuntimes());
                } catch (final Exception e) {
                    throw new RuntimeException("Error while searching for JVMs on the system", e);
                }
            }
        });

        if (foundRuntimes.isEmpty()) {
            LOG.debug("No Java runtime found on your local machine.");
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "No Java runtime found on your local machine.", "Local JVM Search", JOptionPane.INFORMATION_MESSAGE));
        }

        for (Result<LocalJavaRuntime> r : foundRuntimes) {
            if (r.isSuccessful()) {
                final LocalJavaRuntime runtime = r.getResult();
                if (Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(v -> v.contains(runtime.getVersion())).orElse(true)) {
                    add(runtime);
                }
            }
        }
        return Collections.unmodifiableList(foundRuntimes);
    }

    public LocalJavaRuntime install(final RemoteJavaRuntime remoteRuntime, final Consumer<DownloadInputStream> downloadConsumer) throws Exception {
        Assert.requireNonNull(remoteRuntime, "remoteRuntime");

        LOG.debug("Installing remote runtime on local cache");


        if (remoteRuntime.getOperationSystem() != OperationSystem.getLocalSystem()) {
            throw new IllegalArgumentException("Can not install JVM for another os than " + OperationSystem.getLocalSystem().getName());
        }

        final FolderFactory folderFactory = new FolderFactory(cacheBasePath());
        final Path runtimePath = folderFactory.createSubFolder(remoteRuntime.getVendor() + "-" + remoteRuntime.getVersion());

        LOG.debug("Runtime will be installed in {}", runtimePath);

        final URL downloadRequest = remoteRuntime.getEndpoint();
        final HttpGetRequest request = new HttpGetRequest(downloadRequest);
        try (final HttpResponse response = request.handle()) {
            final DownloadInputStream inputStream = new DownloadInputStream(response);

            if (downloadConsumer != null) {
                downloadConsumer.accept(inputStream);
            }
            LOG.debug("Trying to download and extract runtime");
            ZipUtil.unzip(inputStream, runtimePath);
        } catch (final Exception e) {
            try {
                FileUtils.recursiveDelete(runtimePath.toFile(), cacheBaseDir());
            } catch (IOException ex) {
                throw new IOException("Error in Download + Can not delete directory", e);
            }
            throw new IOException("Error in runtime download", e);
        }

        final LocalJavaRuntime newRuntime = LocalJavaRuntime.createManaged(remoteRuntime, runtimePath);

        add(newRuntime);
        return newRuntime;
    }

    public LocalJavaRuntime getBestRuntime(final VersionString versionString, final String vendor) {
        return getBestRuntime(versionString, vendor, OperationSystem.getLocalSystem());
    }

    public LocalJavaRuntime getBestRuntime(final VersionString versionString, final String vendor, final OperationSystem operationSystem) {
        Assert.requireNonNull(versionString, "versionString");
        Assert.requireNonNull(operationSystem, "operationSystem");

        final String vendorName = RuntimeManagerConfig.isNonDefaultVendorsAllowed() && !isBlank(vendor) ? vendor : RuntimeManagerConfig.getDefaultVendor();
        final Vendor vendorForRequest = Vendor.fromString(vendorName);

        LOG.debug("Trying to find local Java runtime. Requested version: '{}' Requested vendor: '{}' requested os: '{}'", versionString, vendorForRequest, operationSystem);

        return runtimes.stream()
                .filter(LocalJavaRuntime::isActive)
                .filter(r -> operationSystem == r.getOperationSystem())
                .filter(r -> Objects.equals(vendorForRequest, ANY_VENDOR) || Objects.equals(vendorForRequest, r.getVendor()))
                .filter(r -> versionString.contains(r.getVersion()))
                .filter(r -> Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(v -> v.contains(r.getVersion())).orElse(true))
                .max(new RuntimeVersionComparator(versionString))
                .orElse(null);
    }

    private File cacheBaseDir() {
        return cacheBasePath().toFile();
    }

    private Path cacheBasePath() {
        return RuntimeManagerConfig.getCachePath();
    }
}
