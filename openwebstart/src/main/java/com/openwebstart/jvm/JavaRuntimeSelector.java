package com.openwebstart.jvm;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.JavaRuntime;
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
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.openwebstart.jvm.RuntimeUpdateStrategy.DO_NOTHING_ON_LOCAL_MATCH;

public class JavaRuntimeSelector implements JavaRuntimeProvider {

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

    @Override
    public LocalJavaRuntime getJavaRuntime(VersionString version, URL url) {
        try {
            return getRuntime(version, url);
        } catch (Exception e) {
            final String msg = "Exception while getting runtime - " + version + " - " + url;
            LOG.info(msg, e);
            DialogFactory.showErrorDialog(msg, e);
            JNLPRuntime.exit(1);
            return null;
        }
    }

    public LocalJavaRuntime getRuntime(final VersionString versionString, final URL serverEndpoint) {
        Assert.requireNonNull(versionString, "versionString");

        LOG.debug("Trying to find Java runtime. Requested version: '{}' Requested url: '{}'", versionString, serverEndpoint);

        final RuntimeUpdateStrategy updateStrategy = RuntimeManagerConfig.getStrategy();
        final String vendorName = RuntimeManagerConfig.getVendor();
        final Vendor vendor = Vendor.fromString(vendorName);
        final OperationSystem os = OperationSystem.getLocalSystem();

        final LocalJavaRuntime localRuntime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);
        if (localRuntime == null) {
            LOG.debug("No local runtime found, will try to find remote runtime");
            final RemoteJavaRuntime remoteJavaRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor, os)
                    .orElseThrow(() -> new RuntimeException("Can not provide or find runtime for version '" +
                            versionString + "', vendor '" + vendor + "' and operating system '" + os + "'"));

            //CHECK IF WE ALREADY HAVE THE SAME RUNTIME (DEACTIVATED & MANAGED) LOCALLY
            final LocalJavaRuntime matchingLocalRuntime = findMatchingDeactivatedAndManagedLocalRuntime(remoteJavaRuntime);
            if (matchingLocalRuntime != null) {
                final boolean useDeactivated = DialogFactory.askForDeactivatedRuntimeUsage(matchingLocalRuntime);
                if (useDeactivated) {
                    return matchingLocalRuntime;
                }
            }

            return installRemoteRuntime(remoteJavaRuntime, serverEndpoint);
        } else if (updateStrategy == DO_NOTHING_ON_LOCAL_MATCH) {
            LOG.debug("Local runtime found and will be used");
            return localRuntime;
        } else {
            LOG.debug("Local runtime found but remote endpoint is checked for newer versions");
            final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor, os)
                    .filter(remoteRuntime -> remoteIsPreferredVersion(versionString, localRuntime, remoteRuntime))
                    .filter(remoteRuntime -> shouldInstallRemoteRuntime(updateStrategy, remoteRuntime))
                    .orElse(null);
            if (runtime == null) {
                return localRuntime;
            }

            //CHECK IF WE ALREADY HAVE THE SAME RUNTIME (DEACTIVATED & MANAGED) LOCALLY
            final LocalJavaRuntime matchingLocalRuntime = findMatchingDeactivatedAndManagedLocalRuntime(runtime);
            if (matchingLocalRuntime != null) {
                final boolean useDeactivated = DialogFactory.askForDeactivatedRuntimeUsage(matchingLocalRuntime);
                if (useDeactivated) {
                    return matchingLocalRuntime;
                }
            }

            return installRemoteRuntime(runtime, serverEndpoint);
        }
    }

    private LocalJavaRuntime findMatchingDeactivatedAndManagedLocalRuntime(final JavaRuntime runtime) {
        return LocalRuntimeManager.getInstance().getAll().stream()
                .filter(l -> !l.isActive())
                .filter(l -> l.isManaged())
                .filter(l -> Objects.equals(l.getVersion(), runtime.getVersion()))
                .filter(l -> Objects.equals(l.getVendor(), runtime.getVendor()))
                .findAny()
                .orElse(null);
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

            return LocalRuntimeManager.getInstance().install(remoteJavaRuntime, serverEndpoint, consumer);
        } catch (IOException e) {
            throw new RuntimeException("Can not install needed runtime", e);
        }
    }

    public static JavaRuntimeSelector getInstance() {
        return INSTANCE;
    }
}
