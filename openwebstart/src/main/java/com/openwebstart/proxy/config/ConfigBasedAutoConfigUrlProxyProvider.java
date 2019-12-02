package com.openwebstart.proxy.config;

import com.openwebstart.proxy.util.pac.AbstractPacBasedProvider;
import com.openwebstart.proxy.util.pac.PacFileEvaluator;
import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

public class ConfigBasedAutoConfigUrlProxyProvider extends AbstractPacBasedProvider {

    private final PacFileEvaluator pacEvaluator;

    public ConfigBasedAutoConfigUrlProxyProvider(final DeploymentConfiguration config) throws Exception {
        pacEvaluator = new PacFileEvaluator(getAutoConfigUrl(config));
    }

    @Override
    protected PacFileEvaluator getPacEvaluator() {
        return pacEvaluator;
    }

    private static URL getAutoConfigUrl(final DeploymentConfiguration config) throws MalformedURLException {
        Assert.requireNonNull(config, "config");
        final String autoConfigUrlProperty = config.getProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL);
        return new URL(autoConfigUrlProperty);
    }
}
