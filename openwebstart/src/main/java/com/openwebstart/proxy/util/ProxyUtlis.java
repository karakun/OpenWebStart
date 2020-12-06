package com.openwebstart.proxy.util;

import com.openwebstart.proxy.ProxyProviderType;
import com.openwebstart.ui.Notifications;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

import static com.openwebstart.config.OwsDefaultsProvider.SHOW_PROXY_UNSUPPORTED_NOTIFICATIONS;

public class ProxyUtlis {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyUtlis.class);

    private static final String DIALOG_MESSAGE = "proxy.unsupportedFeatureDialog.message";

    private static final String MANUAL_PROXY_KEY = "proxy.type.manual.name";

    private static final String PAC_PROXY_KEY = "proxy.type.pac.name";

    private static final String FIREFOX_PROXY_KEY = "proxy.type.firefox.name";

    private static final String SYSTEM_PROXY_KEY = "proxy.type.system.name";

    private static final String UNKNOWN_PROXY_KEY = "proxy.type.unknown.name";

    public static Optional<SocketAddress> getAddress(final String host, final int port) {
        if (host == null || host.trim().isEmpty()) {
            return Optional.empty();
        }
        if (port < 0) {
            return Optional.of(new InetSocketAddress(host, ProxyConstants.FALLBACK_PROXY_PORT));
        }
        return Optional.of(new InetSocketAddress(host, port));
    }

    public static void showUnsupportedFeatureDialog(final DeploymentConfiguration configuration, final ProxyProviderType proxyType, final String featureKey) {
        final String featureName = Translator.getInstance().translate(featureKey);
        final String proxyName = getTranslatedProxyName(proxyType);
        final String message = Translator.getInstance().translate(DIALOG_MESSAGE, proxyName, featureName);
        LOG.warn(message);
        if (Boolean.parseBoolean(configuration.getProperty(SHOW_PROXY_UNSUPPORTED_NOTIFICATIONS))) {
            Notifications.showWarning(message);
        }
    }

    private static String getTranslatedProxyName(final ProxyProviderType proxyType) {
        if (proxyType == ProxyProviderType.MANUAL_HOSTS) {
            return Translator.getInstance().translate(MANUAL_PROXY_KEY);
        } else if (proxyType == ProxyProviderType.MANUAL_PAC_URL) {
            return Translator.getInstance().translate(PAC_PROXY_KEY);
        } else if (proxyType == ProxyProviderType.FIREFOX) {
            return Translator.getInstance().translate(FIREFOX_PROXY_KEY);
        } else if (proxyType == ProxyProviderType.OPERATION_SYSTEM) {
            return Translator.getInstance().translate(SYSTEM_PROXY_KEY);
        } else {
            return Translator.getInstance().translate(UNKNOWN_PROXY_KEY);
        }
    }

}
