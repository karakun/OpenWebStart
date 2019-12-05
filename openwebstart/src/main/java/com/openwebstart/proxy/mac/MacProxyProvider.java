package com.openwebstart.proxy.mac;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.ProxyProviderType;
import com.openwebstart.proxy.ui.ProxyConfigPanel;
import com.openwebstart.proxy.ui.error.ProxyDialogResult;
import com.openwebstart.proxy.ui.error.UnsupportedFeatureDialog;
import com.openwebstart.proxy.util.config.ConfigBasedProvider;
import com.openwebstart.proxy.util.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.util.pac.PacBasedProxyProvider;
import com.openwebstart.util.ProcessUtil;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MacProxyProvider implements ProxyProvider {

    private final static Logger LOG = LoggerFactory.getLogger(MacProxyProvider.class);

    private final ProxyProvider internalProvider;

    public MacProxyProvider() throws IOException, InterruptedException, ExecutionException {
        final Process process = new ProcessBuilder()
                .command("scutil", "--proxy")
                .redirectErrorStream(true)
                .start();

        final Future<String> out = ProcessUtil.getIO(process.getInputStream());
        final int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("process ended with error code " + exitValue);
        }
        final String processOut = out.get();

        final MacProxySettings proxySettings = ScutilParser.parse(processOut);

        if (proxySettings.isAutoDiscoveryEnabled()) {
            final ProxyDialogResult result = new UnsupportedFeatureDialog(ProxyProviderType.OPERATION_SYSTEM, "").showAndWait();
            if(result == ProxyDialogResult.EXIT) {
                LOG.info("Exit app based on missing proxy feature. Please reconfigure the proxy settings");
                JNLPRuntime.exit(-1);
            }
        }
        if (proxySettings.isExcludeSimpleHostnames()) {
            final ProxyDialogResult result = new UnsupportedFeatureDialog(ProxyProviderType.OPERATION_SYSTEM, "").showAndWait();
            if(result == ProxyDialogResult.EXIT) {
                LOG.info("Exit app based on missing proxy feature. Please reconfigure the proxy settings");
                JNLPRuntime.exit(-1);
            }
        }
        if (proxySettings.isFtpPassive()) {
            final ProxyDialogResult result = new UnsupportedFeatureDialog(ProxyProviderType.OPERATION_SYSTEM, "").showAndWait();
            if(result == ProxyDialogResult.EXIT) {
                LOG.info("Exit app based on missing proxy feature. Please reconfigure the proxy settings");
                JNLPRuntime.exit(-1);
            }
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
            internalProvider = new ConfigBasedProvider(proxyConfiguration);
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
