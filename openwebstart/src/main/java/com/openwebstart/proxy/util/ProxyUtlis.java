package com.openwebstart.proxy.util;

import com.openwebstart.proxy.ProxyProviderType;
import com.openwebstart.proxy.ui.error.ProxyDialogResult;
import com.openwebstart.proxy.ui.error.UnsupportedFeatureDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

public class ProxyUtlis {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyUtlis.class);

    public static Optional<SocketAddress> getAddress(final String host, final int port) {
        if (host == null || host.trim().isEmpty()) {
            return Optional.empty();
        }
        if (port < 0) {
            return Optional.of(new InetSocketAddress(host, ProxyConstants.FALLBACK_PROXY_PORT));
        }
        return Optional.of(new InetSocketAddress(host, port));
    }

    public static void showUnsupportedFeatureDialog(final String featureKey) {
        final String featureName = Translator.getInstance().translate(featureKey);
        final ProxyDialogResult result = new UnsupportedFeatureDialog(ProxyProviderType.OPERATION_SYSTEM, featureName).showAndWaitForResult();
        if (result == ProxyDialogResult.EXIT) {
            LOG.info("Exit app based on missing proxy feature. Please reconfigure the proxy settings");
            JNLPRuntime.exit(-1);
        }
    }
}
