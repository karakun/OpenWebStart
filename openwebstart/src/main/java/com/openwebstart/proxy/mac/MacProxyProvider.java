package com.openwebstart.proxy.mac;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.config.ConfigBasedProvider;
import com.openwebstart.proxy.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.pac.PacBasedProxyProvider;
import com.openwebstart.proxy.pac.PacProxyCache;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.openwebstart.proxy.util.ProxyUtlis.showUnsupportedFeatureDialog;

public class MacProxyProvider implements ProxyProvider {

    private static final Set<String> LOCALHOST_INDICATORS = new HashSet<>(Arrays.asList("localhost", "*.local"));

    private final ProxyProvider internalProvider;

    public MacProxyProvider(final DeploymentConfiguration config) throws Exception {

        final MacProxySettings proxySettings = ScutilUtil.executeScutil();

        if (proxySettings.isAutoDiscoveryEnabled()) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.autoDiscovery");
        }
        if (proxySettings.isExcludeSimpleHostnames()) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.excludeSimpleHostnames");
        }
        if (proxySettings.isFtpPassive()) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.ftpPassive");
        }
        if (proxySettings.getHttpUser() != null) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.httpUser");
        }
        if (proxySettings.getHttpsUser() != null) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.httpsUser");
        }
        if (proxySettings.getFtpUser() != null) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.ftpUser");
        }
        if (proxySettings.getSocksUser() != null) {
            showUnsupportedFeatureDialog("proxy.unsupportedFeature.socksUser");
        }

        if (proxySettings.isAutoConfigEnabled()) {
            internalProvider = new PacBasedProxyProvider(proxySettings.getAutoConfigUrl(), PacProxyCache.createFor(config));
        } else {
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
            proxyConfiguration.setBypassLocal(bypassLocalhost(proxySettings));
            internalProvider = new ConfigBasedProvider(proxyConfiguration);
        }
    }

    private boolean bypassLocalhost(MacProxySettings proxySettings) {
        return proxySettings.getExceptionList().stream().anyMatch(LOCALHOST_INDICATORS::contains);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
