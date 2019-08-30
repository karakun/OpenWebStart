package com.openwebstart.jvm;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import com.openwebstart.launcher.JavaHomeProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.openwebstart.jvm.RuntimeUpdateStrategy.DO_NOTHING_ON_LOCAL_MATCH;

public class JavaRuntimeSelector implements JavaHomeProvider {

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
    public Path getJavaHome(VersionString version, URL url) {
        try {
            return getRuntime(version, url).getJavaHome();
        } catch (Exception e) {
            final String msg = "Exception while getting runtime - " + version + " - " + url;
            LOG.info(msg, e);
            try {
                SwingUtilities.invokeAndWait(() -> new ErrorDialog(msg, e).showAndWait());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            JNLPRuntime.exit(1);
            return null;
        }
    }

    public LocalJavaRuntime getRuntime(final VersionString versionString, final URL serverEndpoint) {
        Assert.requireNonNull(versionString, "versionString");

        LOG.debug("Trying to find Java runtime. Requested version: '{}' Requested url: '{}'", versionString, serverEndpoint);

        final RuntimeUpdateStrategy updateStrategy = RuntimeManagerConfig.getStrategy();
        final LocalJavaRuntime localRuntime = LocalRuntimeManager.getInstance().getBestRuntime(versionString);
        if (localRuntime == null) {
            LOG.debug("No local runtime found, will try to find remote runtime");
            final RemoteJavaRuntime remoteJavaRuntime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint).orElseThrow(() -> new RuntimeException("Can not provide or find runtime for version '" + versionString + "' and vendor '" + null + "'"));
            return installRemoteRuntime(remoteJavaRuntime, serverEndpoint);
        } else if (updateStrategy == DO_NOTHING_ON_LOCAL_MATCH) {
            LOG.debug("Local runtime found and will be used");
            return localRuntime;
        } else {
            LOG.debug("Local runtime found but remote endpoint is checked for newer versions");
            return RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint)
                    .filter(remoteRuntime -> remoteIsPreferredVersion(versionString, localRuntime, remoteRuntime))
                    .filter(remoteRuntime -> shouldInstallRemoteRuntime(updateStrategy, remoteRuntime))
                    .map((RemoteJavaRuntime remoteJavaRuntime) -> installRemoteRuntime(remoteJavaRuntime, serverEndpoint))
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
