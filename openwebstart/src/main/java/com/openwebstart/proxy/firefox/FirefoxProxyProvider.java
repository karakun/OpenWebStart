package com.openwebstart.proxy.firefox;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.mac.MacProxyProvider;
import com.openwebstart.proxy.util.config.ConfigBasedProvider;
import com.openwebstart.proxy.util.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.util.pac.PacBasedProxyProvider;
import com.openwebstart.proxy.windows.WindowsProxyProvider;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static com.openwebstart.proxy.firefox.FirefoxConstants.AUTO_CONFIG_URL_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.EXCLUSIONS_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.FTP_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.FTP_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.HTTP_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.HTTP_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.PROXY_TYPE_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.SHARE_SETTINGS_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.SOCKS_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.SOCKS_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.SSL_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.firefox.FirefoxConstants.SSL_PROPERTY_NAME;
import static com.openwebstart.proxy.util.ProxyConstants.DEFAULT_PROTOCOL_PORT;

public class FirefoxProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(FirefoxProxyProvider.class);

    private final ProxyProvider internalProvider;

    public FirefoxProxyProvider() throws Exception {
        final FirefoxPreferences prefs = new FirefoxPreferences();
        prefs.load();

        final int type = prefs.getIntValue(PROXY_TYPE_PROPERTY_NAME, FirefoxProxyType.BROWSER_PROXY_TYPE_SYSTEM.getConfigValue());
        final FirefoxProxyType browserProxyType = FirefoxProxyType.getForConfigValue(type);
        LOG.debug("FireFoxProxyType : {}", browserProxyType);
        if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_PAC) {
            internalProvider = createForPac(prefs);
        } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_MANUAL) {
            internalProvider = createForManualConfig(prefs);
        } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_NONE) {
            internalProvider = DirectProxyProvider.getInstance();
        } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_SYSTEM && OperationSystem.getLocalSystem().isWindows()) {
            internalProvider = new WindowsProxyProvider();
        } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_SYSTEM && OperationSystem.getLocalSystem().isMac()) {
            internalProvider = new MacProxyProvider();
        } else {
            throw new IllegalStateException("Firefox Proxy Type '" + browserProxyType + "' is not supported");
        }
    }

    private ProxyProvider createForManualConfig(final FirefoxPreferences prefs) {
        final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
        proxyConfiguration.setUseHttpForHttpsAndFtp(prefs.getBooleanValue(SHARE_SETTINGS_PROPERTY_NAME, false));
        proxyConfiguration.setUseHttpForSocks(true);
        proxyConfiguration.setHttpHost(prefs.getStringValue(HTTP_PROPERTY_NAME));
        proxyConfiguration.setHttpPort(prefs.getIntValue(HTTP_PORT_PROPERTY_NAME, DEFAULT_PROTOCOL_PORT));
        proxyConfiguration.setHttpsHost(prefs.getStringValue(SSL_PROPERTY_NAME));
        proxyConfiguration.setHttpsPort(prefs.getIntValue(SSL_PORT_PROPERTY_NAME, DEFAULT_PROTOCOL_PORT));
        proxyConfiguration.setFtpHost(prefs.getStringValue(FTP_PROPERTY_NAME));
        proxyConfiguration.setFtpPort(prefs.getIntValue(FTP_PORT_PROPERTY_NAME, DEFAULT_PROTOCOL_PORT));
        proxyConfiguration.setSocksHost(prefs.getStringValue(SOCKS_PROPERTY_NAME));
        proxyConfiguration.setSocksPort(prefs.getIntValue(SOCKS_PORT_PROPERTY_NAME, DEFAULT_PROTOCOL_PORT));
        Arrays.asList(prefs.getStringValue(EXCLUSIONS_PROPERTY_NAME).split("[, ]+"))
                .stream()
                .forEach(v -> proxyConfiguration.addToBypassList(v));

        proxyConfiguration.setBypassLocal(proxyConfiguration.getBypassList().isEmpty());

        return new ConfigBasedProvider(proxyConfiguration);
    }

    private ProxyProvider createForPac(final FirefoxPreferences prefs) throws MalformedURLException {
        final String url = prefs.getStringValue(AUTO_CONFIG_URL_PROPERTY_NAME);
        return new PacBasedProxyProvider(new URL(url));
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
