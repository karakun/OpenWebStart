package com.openwebstart.jvm;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.RuntimeUpdateStrategy;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import com.openwebstart.rico.http.DownloadInputStream;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JavaRuntimeSelector {

    private static final Logger LOG = LoggerFactory.getLogger(JavaRuntimeSelector.class);

    private static final JavaRuntimeSelector INSTANCE = new JavaRuntimeSelector();

    private static BiConsumer<RemoteJavaRuntime, DownloadInputStream> downloadHandler;

    private static Function<RemoteJavaRuntime, Boolean> askForUpdateFunction;

    private JavaRuntimeSelector() {
    }

    public static void setDownloadHandler(final BiConsumer<RemoteJavaRuntime, DownloadInputStream> handler) {
        downloadHandler = handler;
    }

    public static void setAskForUpdateFunction(final Function<RemoteJavaRuntime, Boolean> askForUpdateFunction) {
        JavaRuntimeSelector.askForUpdateFunction = askForUpdateFunction;
    }

    public LocalJavaRuntime getRuntime(final VersionString versionString, final String vendor, final URI serverEndpoint) throws Exception {
        Assert.requireNonNull(versionString, "versionString");

        LOG.debug("Trying to find Java runtime. Requested version: '" + versionString + "' Requested vendor: '" + vendor);


        final RuntimeUpdateStrategy updateStrategy = RuntimeManagerConfig.getInstance().getStrategy();
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
        } else {
            if (Objects.equals(updateStrategy, RuntimeUpdateStrategy.DO_NOTHING_ON_LOCAL_MATCH)) {
                LOG.debug("Local runtime found and will be used");
                return localRuntime;
            } else {
                LOG.debug("Local runtime found but remote endpoint is checked for newer versions");
                return RemoteRuntimeManager.getInstance().getBestRuntime(versionString, serverEndpoint, vendor).map(remoteJavaRuntime -> {
                    if (new RuntimeVersionComparator(versionString).compare(remoteJavaRuntime, localRuntime) > 0) {
                        if (Objects.equals(updateStrategy, RuntimeUpdateStrategy.ASK_FOR_UPDATE_ON_LOCAL_MATCH)) {
                            if (!Optional.ofNullable(askForUpdateFunction).map(f -> f.apply(remoteJavaRuntime)).orElse(true)) {
                                return localRuntime;
                            }
                        }
                        try {
                            LOG.debug("Remote Runtime found. Will install it to local cache");
                            return LocalRuntimeManager.getInstance().install(remoteJavaRuntime, s -> Optional.ofNullable(downloadHandler).ifPresent(h -> h.accept(remoteJavaRuntime, s)));
                        } catch (Exception e) {
                            throw new RuntimeException("Can not install needed runtime", e);
                        }
                    } else {
                        return localRuntime;
                    }
                }).orElse(localRuntime);
            }
        }
    }

    public static JavaRuntimeSelector getInstance() {
        return INSTANCE;
    }
}
