package com.openwebstart.proxy.pac;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.URI;

import static com.openwebstart.config.OwsDefaultsProvider.PROXY_PAC_CACHE;

public interface PacProxyCache {

    String getFromCache(final URI uri);

    void addToCache(final URI uri, final String proxyResult);

    static PacProxyCache createFor(final DeploymentConfiguration configuration) {
        Assert.requireNonNull(configuration, "configuration");
        if (Boolean.parseBoolean(configuration.getProperty(PROXY_PAC_CACHE))) {
            return new DefaultPacProxyCache();
        } else {
            return new NoopPacProxyCache();
        }
    }
}
