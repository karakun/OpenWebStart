package com.openwebstart.proxy.firefox;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.util.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.util.config.SimpleConfigBasedProvider;
import com.openwebstart.proxy.util.pac.PacFileEvaluator;
import com.openwebstart.proxy.util.pac.SimplePacBasedProvider;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static com.openwebstart.proxy.firefox.FirefoxConstants.AUTO_CONFIG_URL_PROPERTY_NAME;
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

public class FirefoxProxyProvider implements ProxyProvider {

    private static final int UNKNOWN_TYPE = -99;

    private final ProxyProvider internalProvider;

    public FirefoxProxyProvider() throws Exception {
        final FirefoxPreferences prefs = new FirefoxPreferences();
        prefs.load();

        final int type = prefs.getIntValue(PROXY_TYPE_PROPERTY_NAME, UNKNOWN_TYPE);
        if (type != UNKNOWN_TYPE) {
            final FirefoxProxyType browserProxyType = FirefoxProxyType.getForConfigValue(type);
            if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_PAC) {
                internalProvider = createForPac(prefs);
            } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_MANUAL) {
                internalProvider = createForManualConfig(prefs);
            } else if (browserProxyType == FirefoxProxyType.BROWSER_PROXY_TYPE_NONE) {
                internalProvider = DirectProxyProvider.getInstance();
            } else {
                throw new IllegalStateException("Firefox Proxy Type '" + browserProxyType + "' is not supported");
            }
        } else {
            //TODO: is this an error or can there be no specification in firefox settings? - Against BROWSER_PROXY_TYPE_NONE
            internalProvider = DirectProxyProvider.getInstance();
        }
    }

    private ProxyProvider createForManualConfig(final FirefoxPreferences prefs) {
        final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
        proxyConfiguration.setUseHttpForHttpsAndFtp(prefs.getBooleanValue(SHARE_SETTINGS_PROPERTY_NAME, false));
        proxyConfiguration.setUseHttpForSocks(true);
        proxyConfiguration.setHttpHost(prefs.getStringValue(HTTP_PROPERTY_NAME));
        proxyConfiguration.setHttpPort(prefs.getIntValue(HTTP_PORT_PROPERTY_NAME, -1));
        proxyConfiguration.setHttpsHost(prefs.getStringValue(SSL_PROPERTY_NAME));
        proxyConfiguration.setHttpsPort(prefs.getIntValue(SSL_PORT_PROPERTY_NAME, -1));
        proxyConfiguration.setFtpHost(prefs.getStringValue(FTP_PROPERTY_NAME));
        proxyConfiguration.setFtpPort(prefs.getIntValue(FTP_PORT_PROPERTY_NAME, -1));
        proxyConfiguration.setSocksHost(prefs.getStringValue(SOCKS_PROPERTY_NAME));
        proxyConfiguration.setSocksPort(prefs.getIntValue(SOCKS_PORT_PROPERTY_NAME, -1));
        return new SimpleConfigBasedProvider(proxyConfiguration);
    }

    private ProxyProvider createForPac(final FirefoxPreferences prefs) throws MalformedURLException {
        final String url = prefs.getStringValue(AUTO_CONFIG_URL_PROPERTY_NAME);
        final URL autoConfigUrl = new URL(url);
        final PacFileEvaluator evaluator = new PacFileEvaluator(autoConfigUrl);
        return new SimplePacBasedProvider(evaluator);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
