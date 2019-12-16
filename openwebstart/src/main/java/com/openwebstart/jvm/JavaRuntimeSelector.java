package com.openwebstart.jvm;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import com.openwebstart.launcher.JavaRuntimeProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.openwebstart.jvm.RuntimeUpdateStrategy.DO_NOTHING_ON_LOCAL_MATCH;

class JavaRuntimeSelector implements JavaRuntimeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JavaRuntimeSelector.class);

    private final BiConsumer<RemoteJavaRuntime, DownloadInputStream> downloadHandler;

    private final Predicate<RemoteJavaRuntime> askForUpdateFunction;

    JavaRuntimeSelector(
            BiConsumer<RemoteJavaRuntime, DownloadInputStream> downloadHandler,
            Predicate<RemoteJavaRuntime> askForUpdateFunction) {
        this.downloadHandler = downloadHandler;
        this.askForUpdateFunction = askForUpdateFunction;
    }

    @Override
    public Optional<LocalJavaRuntime> getJavaRuntime(final VersionString versionString, final URL serverEndpoint) {
        Assert.requireNonNull(versionString, "versionString");

        final RuntimeUpdateStrategy updateStrategy = RuntimeManagerConfig.getStrategy();
        final String vendorName = RuntimeManagerConfig.getVendor();
        final Vendor vendor = Vendor.fromString(vendorName);
        final OperationSystem os = OperationSystem.getLocalSystem();

        LOG.debug("Trying to find Java runtime. Requested version: '{}', vendor: '{}', os: '{}', server-url: '{}'", versionString, vendorName, os, serverEndpoint);

        final Optional<LocalJavaRuntime> localRuntime = LocalRuntimeManager.getInstance().getBestActiveRuntime(versionString, vendor, os);
        if (!localRuntime.isPresent()) {
            LOG.debug("No local runtime found, will try to find remote runtime");
            final Optional<LocalJavaRuntime> installedRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor, os)
                    .map(remoteJavaRuntime -> installRemoteRuntime(remoteJavaRuntime, serverEndpoint));
            if (!installedRuntime.isPresent()) {
                return askForDeactivatedRuntime(versionString, vendor, os);
            }
            return installedRuntime;
        } else if (updateStrategy == DO_NOTHING_ON_LOCAL_MATCH) {
            LOG.debug("Local runtime {} found and will be used", localRuntime);
            return localRuntime;
        } else {
            LOG.debug("Local runtime {} found but remote endpoint is checked for newer versions", localRuntime);
            final Optional<LocalJavaRuntime> installedRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor, os)
                    .filter(remoteRuntime -> remoteIsPreferredVersion(versionString, localRuntime.get(), remoteRuntime))
                    .filter(remoteRuntime -> shouldInstallRemoteRuntime(updateStrategy, remoteRuntime))
                    .map(remoteRuntime -> installRemoteRuntime(remoteRuntime, serverEndpoint));
            if (!installedRuntime.isPresent()) {
                return localRuntime;
            }
            return installedRuntime;
        }
    }

    private Optional<LocalJavaRuntime> askForDeactivatedRuntime(VersionString versionString, Vendor vendor, OperationSystem os) {
        return LocalRuntimeManager.getInstance().getBestDeactivatedRuntime(versionString, vendor, os)
                .filter(DialogFactory::askForDeactivatedRuntimeUsage);
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

    private LocalJavaRuntime installRemoteRuntime(RemoteJavaRuntime remoteJavaRuntime, URL serverEndpoint) {
        try {
            LOG.debug("Remote Runtime found. Will install it to local cache");
            final Consumer<DownloadInputStream> consumer;
            if (downloadHandler != null) {
                consumer = t -> downloadHandler.accept(remoteJavaRuntime, t);
            } else {
                consumer = null;
            }

            return LocalRuntimeManager.getInstance().install(remoteJavaRuntime, consumer);
        } catch (IOException e) {
            throw new RuntimeException("Cannot install needed runtime", e);
        }
    }
}
