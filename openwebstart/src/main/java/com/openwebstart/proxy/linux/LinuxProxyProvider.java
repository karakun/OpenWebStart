package com.openwebstart.proxy.linux;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.config.ConfigBasedProvider;
import com.openwebstart.proxy.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.pac.PacBasedProxyProvider;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.Proxy;
import java.net.URI;
import java.util.List;

import static com.openwebstart.proxy.linux.GnomeProxyConfigReader.readGnomeProxyConfig;
import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.NO_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_USE_AUTHENTICATION;
import static com.openwebstart.proxy.linux.SystemPropertiesProxyConfigReader.readSystemPropertiesProxyConfig;
import static com.openwebstart.proxy.util.ProxyUtlis.showUnsupportedFeatureDialog;

public class LinuxProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxProxyProvider.class);

    private static final LinuxProxySettings NO_PROXY_CONFIG;

    static {
        NO_PROXY_CONFIG = new LinuxProxySettings();
        NO_PROXY_CONFIG.setMode(NO_PROXY);
    }

    private final ProxyProvider internalProvider;


    public LinuxProxyProvider() {

        final LinuxProxySettings proxySettings = readGnomeProxyConfig()
                .orElseGet(() -> readSystemPropertiesProxyConfig()
                        .orElse(NO_PROXY_CONFIG)
                );

        final LinuxProxyProvider.LinuxProxyMode mode = proxySettings.getMode();
        switch (mode) {
            case NO_PROXY:
                internalProvider = DirectProxyProvider.getInstance();
                break;
            case PAC:
                internalProvider = new PacBasedProxyProvider(proxySettings.getAutoConfigUrl());
                break;
            case MANUAL:
                if (proxySettings.isAuthenticationEnabled()) {
                    showUnsupportedFeatureDialog("proxy.unsupportedFeature.httpUser");
                }
                final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
                if (proxySettings.isHttpEnabled()) {
                    proxyConfiguration.setHttpHost(proxySettings.getHttpHost());
                    proxyConfiguration.setHttpPort(proxySettings.getHttpPort());
                }
                if (proxySettings.isHttpsEnabled()) {
                    proxyConfiguration.setHttpsHost(proxySettings.getHttpsHost());
                    proxyConfiguration.setHttpsPort(proxySettings.getHttpsPort());
                }
                if (proxySettings.isFtpEnabled()) {
                    proxyConfiguration.setFtpHost(proxySettings.getFtpHost());
                    proxyConfiguration.setFtpPort(proxySettings.getFtpPort());
                }
                if (proxySettings.isSocksEnabled()) {
                    proxyConfiguration.setSocksHost(proxySettings.getSocksHost());
                    proxyConfiguration.setSocksPort(proxySettings.getSocksPort());
                }
                proxySettings.getExceptionList().forEach(proxyConfiguration::addToBypassList);
                proxyConfiguration.setBypassLocal(proxySettings.isLocalhostExcluded());
                internalProvider = new ConfigBasedProvider(proxyConfiguration);
            default:
                throw new IllegalArgumentException("unknown linux proxy mode: " + mode);
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }

    enum LinuxProxyMode {
        NO_PROXY, PAC, MANUAL
    }
}
