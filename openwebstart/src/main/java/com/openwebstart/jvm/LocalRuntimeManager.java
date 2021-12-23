package com.openwebstart.jvm;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.func.Result;
import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.http.HttpGetRequest;
import com.openwebstart.http.HttpResponse;
import com.openwebstart.jvm.json.CacheStore;
import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.listener.RuntimeAddedListener;
import com.openwebstart.jvm.listener.RuntimeRemovedListener;
import com.openwebstart.jvm.listener.RuntimeUpdateListener;
import com.openwebstart.jvm.localfinder.JdkFinder;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import com.openwebstart.mimetype.MimeType;
import com.openwebstart.mimetype.MimeTypeInputStream;
import com.openwebstart.util.ExtractUtil;
import com.openwebstart.util.FolderFactory;
import com.openwebstart.util.Subscription;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.openwebstart.config.OwsDefaultsProvider.JVM_CACHE_CLEANUP_ENABLED;
import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;
import static java.lang.Boolean.parseBoolean;
import static java.time.temporal.ChronoUnit.DAYS;
import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;

public final class LocalRuntimeManager {

    public static final String JVM_FOLDER_SUFFIX = "_x32";
    private static final Logger LOG = LoggerFactory.getLogger(LocalRuntimeManager.class);

    private static final LocalRuntimeManager INSTANCE = new LocalRuntimeManager();

    private final List<LocalJavaRuntime> runtimes = new CopyOnWriteArrayList<>();

    private final List<RuntimeRemovedListener> removedListeners = new CopyOnWriteArrayList<>();
    private final List<RuntimeAddedListener> addedListeners = new CopyOnWriteArrayList<>();
    private final List<RuntimeUpdateListener> updatedListeners = new CopyOnWriteArrayList<>();

    private final Lock jsonStoreLock = new ReentrantLock();

    private final AtomicBoolean firstTimeLoading = new AtomicBoolean(true);

    private LocalRuntimeManager() {
    }

    public List<LocalJavaRuntime> getAll() {
        return Collections.unmodifiableList(runtimes);
    }

    public Subscription addRuntimeAddedListener(final RuntimeAddedListener listener) {
        addedListeners.add(listener);
        return () -> addedListeners.remove(listener);
    }

    public Subscription addRuntimeRemovedListener(final RuntimeRemovedListener listener) {
        removedListeners.add(listener);
        return () -> removedListeners.remove(listener);
    }

    public Subscription addRuntimeUpdatedListener(final RuntimeUpdateListener listener) {
        updatedListeners.add(listener);
        return () -> updatedListeners.remove(listener);
    }

    private void saveRuntimes() {
        jsonStoreLock.lock();
        try {
            LOG.debug("Saving runtime cache to filesystem");
            final File cachePath = cacheBaseDir();
            if (!cachePath.exists()) {
                final boolean dirCreated = cachePath.mkdirs();
                if (!dirCreated) {
                    throw new IOException("Cannot create cache dir '" + cachePath + "'");
                }
            }
            final File jsonFile = new File(cachePath, RuntimeManagerConstants.JSON_STORE_FILENAME);
            if (jsonFile.exists() && !jsonFile.delete()) {
                // if the file is locked, try again after sometime
                LOG.debug("Could not delete {}. File maybe locked. Trying again.", RuntimeManagerConstants.JSON_STORE_FILENAME);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
                if (jsonFile.exists() && !jsonFile.delete()) {
                    throw new IOException("Unable to delete old config file!");
                }
            }
            final CacheStore cacheStore = new CacheStore(runtimes);
            final String jsonString = JsonHandler.getInstance().toJson(cacheStore);
            FileUtils.saveFileUtf8(jsonString, jsonFile);
        } catch (final Exception e) {
            throw new RuntimeException("Error while saving JVM cache.", e);
        } finally {
            jsonStoreLock.unlock();
        }
    }

    /**
     * Load runtimes from filesystem into cache.
     * <p>
     * Do some housekeeping:
     * <ul>
     *     <li>Remove runtime if no longer present on the file system</li>
     *     <li>Remove runtime if it is considered as unused</li>
     * </ul>
     */
    void loadRuntimes(DeploymentConfiguration configuration) {
        LOG.debug("Loading runtime cache from filesystem");
        jsonStoreLock.lock();
        final File jsonFile = new File(cacheBaseDir(), RuntimeManagerConstants.JSON_STORE_FILENAME);
        try {
            clearInMemory();
            if (jsonFile.exists()) {
                final String content = FileUtils.loadFileAsUtf8String(jsonFile);
                final CacheStore cacheStore = JsonHandler.getInstance().fromJson(content, CacheStore.class);
                final List<LocalJavaRuntime> runtimesFromFile = cacheStore.getRuntimes();

                runtimesFromFile.forEach(this::loadIntoMemory);
                cleanupJvmCacheFile(runtimesFromFile);
            }

            final boolean isFirstTimeLoading = firstTimeLoading.getAndSet(false);
            if (isFirstTimeLoading) {
                findAndAddNewLocalRuntimes(configuration);
            }
        } catch (IOException e) {
            LOG.error("Could not load file: {}", jsonFile);
            throw new RuntimeException(e);
        } finally {
            jsonStoreLock.unlock();
        }
    }

    private void cleanupJvmCacheFile(List<LocalJavaRuntime> cacheFileContent) {
        final boolean jvmCleanupDisabled = !parseBoolean(getConfiguration().getProperty(JVM_CACHE_CLEANUP_ENABLED));
        if (jvmCleanupDisabled) {
            return;
        }

        cacheFileContent.stream()
                .filter(LocalJavaRuntime::isManaged)
                .filter(this::isUnused)
                .forEach(this::removeInMemory);

        if (!Objects.equals(cacheFileContent, runtimes)) {
            saveRuntimes();
        }
    }

    private boolean isUnused(final LocalJavaRuntime localJavaRuntime) {
        final int maxDaysUnusedInJvmCache = RuntimeManagerConfig.getMaxDaysUnusedInJvmCache();
        final LocalDateTime lastUsage = localJavaRuntime.getLastUsage();
        final long daysSinceLastUsage = DAYS.between(lastUsage, LocalDateTime.now());

        if (daysSinceLastUsage > maxDaysUnusedInJvmCache) {
            LOG.info("Runtime '{}' is unused as it has not been touched since {} days", localJavaRuntime.getJavaHome(), daysSinceLastUsage);
            return true;
        }
        return false;
    }

    private boolean isJvmMissing(final LocalJavaRuntime localJavaRuntime) {
        final Path javaHome = localJavaRuntime.getJavaHome();
        final Path javaRuntimePath = Paths.get(javaHome.toString(), "bin", OsUtil.isWindows() ? "java.exe" : "java");

        return !Files.exists(javaHome) || !Files.isDirectory(javaHome) || !Files.exists(javaRuntimePath);
    }

    private void loadIntoMemory(final LocalJavaRuntime localJavaRuntime) {
        if (localJavaRuntime == null || runtimes.contains(localJavaRuntime) || isJvmMissing(localJavaRuntime)) {
            return;
        }

        runtimes.add(localJavaRuntime);
        addedListeners.forEach(l -> l.onRuntimeAdded(localJavaRuntime));
    }

    private void clearInMemory() {
        LOG.debug("Clearing runtime cache");
        runtimes.forEach(r -> {
            if (runtimes.remove(r)) {
                removedListeners.forEach(l -> l.onRuntimeRemoved(r));
            }
        });
    }

    public void replace(final LocalJavaRuntime oldRuntime, final LocalJavaRuntime newRuntime) {
        LOG.debug("Replacing runtime definition with new one");

        Assert.requireNonNull(oldRuntime, "oldRuntime");
        Assert.requireNonNull(newRuntime, "newRuntime");

        if (!Objects.equals(oldRuntime.getJavaHome(), newRuntime.getJavaHome())) {
            throw new IllegalArgumentException("Can only replace a runtime with same JAVA_HOME");
        }

        if (!Objects.equals(oldRuntime.isManaged(), newRuntime.isManaged())) {
            throw new IllegalArgumentException("Cannot change managed state of runtime");
        }

        final int index = runtimes.indexOf(oldRuntime);
        if (index < 0) {
            throw new IllegalArgumentException("Item is not in collection!");
        }
        runtimes.remove(index);
        runtimes.add(index, newRuntime);
        updatedListeners.forEach(l -> l.onRuntimeUpdated(oldRuntime, newRuntime));

        saveRuntimes();
    }

    private void findAndAddNewLocalRuntimes(DeploymentConfiguration configuration) {
        final String searchOnStartValue = configuration.getProperty(OwsDefaultsProvider.SEARCH_FOR_LOCAL_JVM_ON_STARTUP);
        if (parseBoolean(searchOnStartValue)) {
            final List<LocalJavaRuntime> found = JdkFinder.findLocalRuntimes(configuration)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(Result::isSuccessful)
                    .map(Result::getResult)
                    .collect(Collectors.toList());

            addNewLocalJavaRuntime(found, s -> {});
        }
    }

    public int addNewLocalJavaRuntime(List<LocalJavaRuntime> newRuntimes, Consumer<String> errorMessageHandler) {
        Assert.requireNonNull(newRuntimes, "runtimes");
        int numAdded = 0;

        for (LocalJavaRuntime newRuntime : newRuntimes) {
            if (supportsVersionRange(newRuntime)) {
                try {
                    if (addNewRuntimeInMemory(newRuntime)) {
                        numAdded++;
                    }
                } catch (final Exception e) {
                    LOG.error("Error while adding local JDK at '" + newRuntime.getJavaHome() + "'", e);
                    errorMessageHandler.accept(Translator.getInstance().translate("jvmManager.error.jvmNotAdded"));
                }
            } else {
                LOG.error("JVM at '" + newRuntime.getJavaHome() + "' has unsupported version '" + newRuntime.getVersion() + "'. Allowed Range: '" + RuntimeManagerConfig.getSupportedVersionRange() + "'");
                errorMessageHandler.accept(Translator.getInstance().translate("jvmManager.error.versionOutOfRange"));
            }
        }
        if (numAdded > 0) {
            saveRuntimes();
        }
        return numAdded;
    }

    private boolean supportsVersionRange(final LocalJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");
        final VersionId version = runtime.getVersion();
        return Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange())
                .map(v -> v.contains(version))
                .orElse(true);
    }

    private boolean addNewRuntimeInMemory(final LocalJavaRuntime localJavaRuntime) {
        LOG.debug("Adding runtime definition");

        Assert.requireNonNull(localJavaRuntime, "localJavaRuntime");

        if (isJvmMissing(localJavaRuntime)) {
            throw new IllegalArgumentException("Cannot add invalid runtime with JAVAHOME=" + localJavaRuntime.getJavaHome());
        }

        if (runtimes.contains(localJavaRuntime)) {
            LOG.debug("Runtime already known - will not add");
            return false;
        }

        removeRuntimesByJavaHome(localJavaRuntime.getJavaHome());
        runtimes.add(localJavaRuntime);
        addedListeners.forEach(l -> l.onRuntimeAdded(localJavaRuntime));

        return true;
    }

    private void removeRuntimesByJavaHome(Path javaHome) {

        runtimes.stream()
                .filter(rt -> Objects.equals(rt.getJavaHome(), javaHome))
                .forEach(rt -> {
                    runtimes.remove(rt);
                    removedListeners.forEach(l -> l.onRuntimeRemoved(rt));
                });
    }

    public void remove(final LocalJavaRuntime localJavaRuntime) {
        Assert.requireNonNull(localJavaRuntime, "localJavaRuntime");

        LOG.debug("Removing runtime definition");

        if (removeInMemory(localJavaRuntime)) {
            saveRuntimes();
        }
    }

    public void removeAll(final List<LocalJavaRuntime> localJavaRuntimes) {
        Assert.requireNonNull(localJavaRuntimes, "localJavaRuntimes");

        LOG.debug("Removing all runtime definition");

        final long numRemoved = localJavaRuntimes.stream()
                .filter(this::removeInMemory)
                .count();

        if (numRemoved > 0) {
            saveRuntimes();
        }
    }

    private boolean removeInMemory(final LocalJavaRuntime localJavaRuntime) {
        if (runtimes.remove(localJavaRuntime)) {
            if (localJavaRuntime.isManaged()) {
                final Path runtimeDir = localJavaRuntime.getJavaHome();
                try {
                    FileUtils.recursiveDelete(runtimeDir.toFile(), cacheBaseDir());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            removedListeners.forEach(l -> l.onRuntimeRemoved(localJavaRuntime));
            return true;
        }
        return false;
    }

    public static void touch(final LocalJavaRuntime currentRuntime) {
        final boolean jvmCleanupDisabled = !parseBoolean(getConfiguration().getProperty(JVM_CACHE_CLEANUP_ENABLED));
        final boolean isNotManagedByOws = !currentRuntime.isManaged();
        if (jvmCleanupDisabled || isNotManagedByOws) {
            LOG.debug("Runtime cache is currently read only, not saving.");
            return;
        }

        LocalJavaRuntime newRuntime = new LocalJavaRuntime(
                currentRuntime.getVersion().toString(),
                currentRuntime.getOperationSystem(),
                currentRuntime.getVendor().toString(),
                currentRuntime.getJavaHome(),
                LocalDateTime.now(),
                currentRuntime.isActive(),
                currentRuntime.isManaged()
        );

        LocalRuntimeManager.getInstance().replace(currentRuntime, newRuntime);
    }

    public static LocalRuntimeManager getInstance() {
        return INSTANCE;
    }

    LocalJavaRuntime install(final RemoteJavaRuntime remoteRuntime, final Consumer<DownloadInputStream> downloadConsumer) throws IOException {
        Assert.requireNonNull(remoteRuntime, "remoteRuntime");

        LOG.debug("Installing remote runtime {} on local cache", remoteRuntime);

        if (canInstallJVMOnOS(remoteRuntime) == false) {
            throw new IllegalArgumentException("Cannot install JVM for another os than " + OperationSystem.getLocalSystem().getName());
        }

        final FolderFactory folderFactory = new FolderFactory(cacheBasePath(), true);
        final Path runtimePath = folderFactory.createSubFolder(remoteRuntime.getVendor().getShortName() + "_" + remoteRuntime.getVersion() + (remoteRuntime.getOperationSystem().is32Bit() ? JVM_FOLDER_SUFFIX : "" ));

        LOG.info("Runtime {} will be installed in {}", remoteRuntime.getHref(), runtimePath);

        final URL downloadRequest = remoteRuntime.getEndpoint();
        final HttpGetRequest request = new HttpGetRequest(downloadRequest);
        try (final HttpResponse response = request.handle()) {
            final DownloadInputStream inputStream = new DownloadInputStream(response);

            if (downloadConsumer != null) {
                downloadConsumer.accept(inputStream);
            }
            LOG.info("Trying to download and extract runtime {}", remoteRuntime.getHref());

            MimeTypeInputStream wrappedStream = new MimeTypeInputStream(inputStream);
            final MimeType mimeType = wrappedStream.getMimeType();
            if (MimeType.ZIP == mimeType) {
                LOG.info("Remote runtime is distributed as ZIP. Will extract it");
                ExtractUtil.unZip(wrappedStream, runtimePath);
            } else if (MimeType.GZIP == mimeType) {
                LOG.info("Remote runtime is distributed as GZIP. Will extract it");
                ExtractUtil.unTarGzip(wrappedStream, runtimePath); //We assume that GZIP is always a tar.gz
            } else {
                throw new IllegalStateException("The remote runtime is distributed in an unknown mimetype.");
            }
        } catch (final Exception e) {
            LOG.error("Error in runtime {} download: {}", remoteRuntime.getHref(), e.getMessage());
            try {
                FileUtils.recursiveDelete(runtimePath.toFile(), cacheBaseDir());
            } catch (IOException ex) {
                throw new IOException("Error in Download + Cannot delete directory", e);
            }
            throw new IOException("Error in runtime download", e);
        }
        LOG.info("Remote runtime {} successfully installed in {}", remoteRuntime.getHref(), runtimePath);
        final LocalJavaRuntime newRuntime = LocalJavaRuntime.createManaged(remoteRuntime, runtimePath);

        if (addNewRuntimeInMemory(newRuntime)) {
            saveRuntimes();
            return newRuntime;
        } else {
            return runtimes.stream()
                    .filter(rt -> Objects.equals(rt, newRuntime))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cannot add local runtime and cannot find it in memory either. Please restart OpenWebStart"));
        }
    }

    private boolean canInstallJVMOnOS(RemoteJavaRuntime remoteRuntime) {
        if (OperationSystem.getLocalSystem().isWindows() && remoteRuntime.getOperationSystem().isWindows() &&  isArchitectureCompatible(remoteRuntime)) {
            return true;
        } else if (OperationSystem.getLocalSystem().isLinux() && remoteRuntime.getOperationSystem().isLinux() &&  isArchitectureCompatible(remoteRuntime)) {
            return true;
        } else {
            return OperationSystem.getLocalSystem() == remoteRuntime.getOperationSystem();
        }
    }

    private boolean isArchitectureCompatible(final RemoteJavaRuntime remoteRuntime) {
        return (OperationSystem.getLocalSystem().is64Bit()) ||
                (OperationSystem.getLocalSystem().is32Bit() && remoteRuntime.getOperationSystem().is32Bit());
    }

    Optional<LocalJavaRuntime> getBestActiveRuntime(final VersionString versionString, final Vendor vendor, final OperationSystem operationSystem) {
        return findBestRuntime(versionString, vendor, operationSystem, true);
    }

    Optional<LocalJavaRuntime> getBestDeactivatedRuntime(final VersionString versionString, final Vendor vendor, final OperationSystem operationSystem) {
        return findBestRuntime(versionString, vendor, operationSystem, false);
    }

    private Optional<LocalJavaRuntime> findBestRuntime(VersionString versionString, Vendor vendor, OperationSystem operationSystem, boolean active) {
        Assert.requireNonNull(versionString, "versionString");
        Assert.requireNonNull(vendor, "vendor");
        Assert.requireNonNull(operationSystem, "operationSystem");

        LOG.debug("Trying to find local Java runtime. Requested version: '{}' Requested vendor: '{}' requested os: '{}' active: '{}'",
                versionString, vendor, operationSystem, active);

        return runtimes.stream()
                .filter(r -> r.isActive() == active)
                .filter(r -> Objects.equals(vendor, ANY_VENDOR) || Objects.equals(vendor, r.getVendor()))
                .filter(r -> versionString.contains(r.getVersion()))
                .filter(r -> Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(v -> v.contains(r.getVersion())).orElse(true))
                .filter(r -> operationSystem == r.getOperationSystem())
                .max(new RuntimeVersionComparator(versionString));
    }

    boolean hasManagedRuntime(final VersionId versionId, final Vendor vendor, final OperationSystem os) {
        return LocalRuntimeManager.getInstance().getAll().stream()
                .filter(LocalJavaRuntime::isManaged)
                .filter(l -> Objects.equals(l.getVersion(), versionId))
                .filter(l -> Objects.equals(l.getVendor(), vendor))
                .anyMatch(l -> Objects.equals(l.getOperationSystem(), os));
    }

    private File cacheBaseDir() {
        return cacheBasePath().toFile();
    }

    private Path cacheBasePath() {
        return RuntimeManagerConfig.getCachePath();
    }
}
