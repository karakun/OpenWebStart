package com.openwebstart.proxy.config;

import com.openwebstart.proxy.util.config.AbstractConfigBasedProvider;
import com.openwebstart.proxy.util.config.ProxyConfiguration;
import com.openwebstart.proxy.util.config.ProxyConfigurationImpl;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import static com.openwebstart.proxy.util.ProxyConstants.DEFAULT_PROTOCOL_PORT;
import static java.lang.Boolean.parseBoolean;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LIST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_FTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_FTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTPS_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTPS_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SAME;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SOCKS4_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SOCKS4_PORT;

public class ConfigBasedProxyProvider extends AbstractConfigBasedProvider {

    private final ProxyConfiguration proxyConfiguration;

    public ConfigBasedProxyProvider(final DeploymentConfiguration config) {
        this.proxyConfiguration = createConfiguration(config);
    }

    @Override
    protected ProxyConfiguration getConfig() {
        return proxyConfiguration;
    }

    private static ProxyConfiguration createConfiguration(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");

        final ProxyConfigurationImpl result = new ProxyConfigurationImpl();

        result.setBypassLocal(parseBoolean(config.getProperty(KEY_PROXY_BYPASS_LOCAL)));
        result.setUseHttpForHttpsAndFtp(parseBoolean(config.getProperty(KEY_PROXY_SAME)));
        result.setHttpHost(toHost(config.getProperty(KEY_PROXY_HTTP_HOST)));
        result.setHttpPort(toPort(config.getProperty(KEY_PROXY_HTTP_PORT)));
        result.setHttpsHost(toHost(config.getProperty(KEY_PROXY_HTTPS_HOST)));
        result.setHttpsPort(toPort(config.getProperty(KEY_PROXY_HTTPS_PORT)));
        result.setFtpHost(toHost(config.getProperty(KEY_PROXY_FTP_HOST)));
        result.setFtpPort(toPort(config.getProperty(KEY_PROXY_FTP_PORT)));
        result.setSocksHost(toHost(config.getProperty(KEY_PROXY_SOCKS4_HOST)));
        result.setSocksPort(toPort(config.getProperty(KEY_PROXY_SOCKS4_PORT)));

        config.getPropertyAsList(KEY_PROXY_BYPASS_LIST, ',').stream()
                .filter(host -> !StringUtils.isBlank(host))
                .forEach(result::addToBypassList);

        return result;
    }

    private static String toHost(String host) {
        return !StringUtils.isBlank(host) ? host.trim() : null;
    }

    private static int toPort(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_PROTOCOL_PORT;
        }
    }

}
