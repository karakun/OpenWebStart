package com.openwebstart.proxy.mac;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.util.config.ConfigBasedProvider;
import com.openwebstart.proxy.util.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.util.pac.PacBasedProxyProvider;
import com.openwebstart.util.ProcessUtil;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MacProxyProvider implements ProxyProvider {

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
            //TODO: Notify User that unsupported proxy settings are configured!
        }
        if (proxySettings.isExcludeSimpleHostnames()) {
            //TODO: Notify User that unsupported proxy settings are configured!
        }
        if (proxySettings.isFtpPassive()) {
            //TODO: Notify User that unsupported proxy settings are configured!
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
        return null;
    }
}
