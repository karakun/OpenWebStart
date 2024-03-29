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
import static com.openwebstart.jvm.RuntimeUpdateStrategy.NO_REMOTE;

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
    public Optional<LocalJavaRuntime> getJavaRuntime(final VersionString versionString, final Vendor vendorFromJnlp, final URL serverEndpointFromJnlp, final boolean require32bit) {
        Assert.requireNonNull(versionString, "versionString");

        LOG.debug("requested: JRE with version string '{}' and vendor '{}', require 32 bit = {}, from {}", versionString, vendorFromJnlp, require32bit, serverEndpointFromJnlp);

        final RuntimeUpdateStrategy updateStrategy = RuntimeManagerConfig.getStrategy();
        final Vendor vendor = Optional.ofNullable(vendorFromJnlp)
                .filter(v -> isVendorFromJnlpAllowed())
                .orElseGet(() -> Vendor.fromStringOrAny(RuntimeManagerConfig.getVendor()));

        final OperationSystem os = getOperationSystem(require32bit);

        LOG.debug("Trying to find local Java runtime. Requested version: '{}', vendor: '{}', os: '{}'", versionString, vendor, os);

        final Optional<LocalJavaRuntime> localRuntime = LocalRuntimeManager.getInstance().getBestActiveRuntime(versionString, vendor, os);

        if (!localRuntime.isPresent()) {
            if (updateStrategy == NO_REMOTE) {
                LOG.debug("No local runtime found and '{}' strategy prevents remote lookup", NO_REMOTE);
                return Optional.empty();
            }
            LOG.debug("No local runtime found, will try to find remote runtime");

            final Optional<LocalJavaRuntime> installedRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpointFromJnlp, vendor, os)
                    .map(this::installRemoteRuntime);
            if (!installedRuntime.isPresent()) {
                LOG.debug("No remote runtime found, will check deactivated local runtimes.");
                return askForDeactivatedRuntime(versionString, vendor, os);
            }
            return installedRuntime;
        } else if (updateStrategy == DO_NOTHING_ON_LOCAL_MATCH || updateStrategy == NO_REMOTE) {
            LOG.debug("Local runtime {} found and will be used", localRuntime.get());
            return localRuntime;
        } else {
            LOG.debug("Local runtime {} found but remote endpoint is checked for newer versions", localRuntime.get());
            final Optional<LocalJavaRuntime> installedRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpointFromJnlp, vendor, os)
                    .filter(remoteRuntime -> remoteIsPreferredVersion(versionString, localRuntime.get(), remoteRuntime))
                    .filter(remoteRuntime -> shouldInstallRemoteRuntime(updateStrategy, remoteRuntime))
                    .map(this::installRemoteRuntime);
            if (!installedRuntime.isPresent()) {
                LOG.debug("No newer version was installed");
                return localRuntime;
            }
            LOG.debug("Newer runtime {} installed", installedRuntime.get());
            return installedRuntime;
        }
    }

    private boolean isVendorFromJnlpAllowed() {
        if (RuntimeManagerConfig.isVendorFromJnlpAllowed()) {
            return true;
        }
        final Vendor vendor = Vendor.fromStringOrAny(RuntimeManagerConfig.getVendor());
        return Vendor.ANY_VENDOR.equals(vendor);
    }

    private OperationSystem getOperationSystem(boolean require32bit) {
        final OperationSystem os = OperationSystem.getLocalSystem();

        if (require32bit) {
            return os.getVariant32bit();
        }

        return os;
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

    private LocalJavaRuntime installRemoteRuntime(RemoteJavaRuntime remoteJavaRuntime) {
        try {
            LOG.debug("Remote Runtime {} found. Will install it to local cache", remoteJavaRuntime.getHref());
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
