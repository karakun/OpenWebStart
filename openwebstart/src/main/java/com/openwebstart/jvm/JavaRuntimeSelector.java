package com.openwebstart.jvm;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.openwebstart.jvm.RuntimeUpdateStrategy.DO_NOTHING_ON_LOCAL_MATCH;

public class JavaRuntimeSelector {

    private static final Logger LOG = LoggerFactory.getLogger(JavaRuntimeSelector.class);

    private static final JavaRuntimeSelector INSTANCE = new JavaRuntimeSelector();

    private BiConsumer<RemoteJavaRuntime, DownloadInputStream> downloadHandler;
    private Predicate<RemoteJavaRuntime> askForUpdateFunction;

    private JavaRuntimeSelector() {
    }

    public static void setDownloadHandler(final BiConsumer<RemoteJavaRuntime, DownloadInputStream> downloadHandler) {
        getInstance().downloadHandler = downloadHandler;
    }

    public static void setAskForUpdateFunction(final Predicate<RemoteJavaRuntime> askForUpdateFunction) {
        getInstance().askForUpdateFunction = askForUpdateFunction;
    }

    public LocalJavaRuntime getRuntime(final VersionString versionString, final String vendor, final URL serverEndpoint) throws Exception {
        Assert.requireNonNull(versionString, "versionString");

        LOG.debug("Trying to find Java runtime. Requested version: '" + versionString + "' Requested vendor: '" + vendor);

        final RuntimeUpdateStrategy updateStrategy = RuntimeManagerConfig.getStrategy();
        final LocalJavaRuntime localRuntime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor);
        if (localRuntime == null) {
            LOG.debug("No local runtime found, will try to find remote runtime");
            final RemoteJavaRuntime remoteJavaRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor).orElseThrow(() -> new RuntimeException("Can not provide or find runtime for version '" + versionString + "' and vendor '" + vendor + "'"));
            try {
                LOG.debug("Remote Runtime found. Will install it to local cache");
                return LocalRuntimeManager.getInstance().install(remoteJavaRuntime, s -> Optional.ofNullable(downloadHandler).ifPresent(h -> h.accept(remoteJavaRuntime, s)));
            } catch (final Exception e) {
                throw new RuntimeException("Can not install needed runtime", e);
            }
        } else if (updateStrategy == DO_NOTHING_ON_LOCAL_MATCH) {
            LOG.debug("Local runtime found and will be used");
            return localRuntime;
        } else {
            LOG.debug("Local runtime found but remote endpoint is checked for newer versions");
            return RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor)
                    .filter(remoteRuntime -> remoteIsPreferredVersion(versionString, localRuntime, remoteRuntime))
                    .filter(remoteRuntime -> shouldInstallRemoteRuntime(updateStrategy, remoteRuntime))
                    .map(this::installRemoteRuntime)
                    .orElse(localRuntime);
        }
    }

    private boolean remoteIsPreferredVersion(VersionString versionString, LocalJavaRuntime localRuntime, RemoteJavaRuntime remoteRuntime) {
        return new RuntimeVersionComparator(versionString).compare(remoteRuntime, localRuntime) > 0;
    }

    private boolean shouldInstallRemoteRuntime(RuntimeUpdateStrategy updateStrategy, RemoteJavaRuntime remoteRuntime) {
        if (updateStrategy == RuntimeUpdateStrategy.AUTOMATICALLY_DOWNLOAD) {
            return true;
        }
        if (updateStrategy == RuntimeUpdateStrategy.ASK_FOR_UPDATE_ON_LOCAL_MATCH) {
            return askForUpdateFunction == null || askForUpdateFunction.test(remoteRuntime);
        }
        return false;
    }

    private LocalJavaRuntime installRemoteRuntime(RemoteJavaRuntime remoteJavaRuntime) {
        try {
            LOG.debug("Remote Runtime found. Will install it to local cache");

            final Consumer<DownloadInputStream> consumer;
            if (downloadHandler != null) {
                consumer = t -> downloadHandler.accept(remoteJavaRuntime, t);
            } else {
                consumer = null;
            }

            return LocalRuntimeManager.getInstance().install(remoteJavaRuntime, consumer);
        } catch (Exception e) {
            throw new RuntimeException("Can not install needed runtime", e);
        }
    }

    public static JavaRuntimeSelector getInstance() {
        return INSTANCE;
    }
}
