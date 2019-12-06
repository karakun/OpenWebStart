package com.openwebstart.proxy.mac;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.ProxyProviderType;
import com.openwebstart.proxy.config.ConfigBasedProvider;
import com.openwebstart.proxy.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.pac.PacBasedProxyProvider;
import com.openwebstart.proxy.ui.error.ProxyDialogResult;
import com.openwebstart.proxy.ui.error.UnsupportedFeatureDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MacProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MacProxyProvider.class);

    private static final Set<String> LOCALHOST_INDICATORS = new HashSet<>(Arrays.asList("localhost", "*.local"));

    private final ProxyProvider internalProvider;

    public MacProxyProvider() throws IOException, InterruptedException, ExecutionException {

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
            internalProvider = new PacBasedProxyProvider(proxySettings.getAutoConfigUrl());
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

    private void showUnsupportedFeatureDialog(final String featureKey) {
        final String featureName = Translator.getInstance().translate(featureKey);
        final ProxyDialogResult result = new UnsupportedFeatureDialog(ProxyProviderType.OPERATION_SYSTEM, featureName).showAndWait();
        if (result == ProxyDialogResult.EXIT) {
            LOG.info("Exit app based on missing proxy feature. Please reconfigure the proxy settings");
            JNLPRuntime.exit(-1);
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
