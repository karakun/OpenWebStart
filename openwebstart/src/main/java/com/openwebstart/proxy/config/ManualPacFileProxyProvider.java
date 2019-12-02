package com.openwebstart.proxy.config;

import com.openwebstart.proxy.util.pac.PacBasedProxyProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

public class ManualPacFileProxyProvider extends PacBasedProxyProvider {

    public ManualPacFileProxyProvider(final DeploymentConfiguration config) throws Exception {
        super(getAutoConfigUrl(config));
    }

    private static URL getAutoConfigUrl(final DeploymentConfiguration config) throws MalformedURLException {
        Assert.requireNonNull(config, "config");
        final String autoConfigUrlProperty = config.getProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL);
        return new URL(autoConfigUrlProperty);
    }
}
