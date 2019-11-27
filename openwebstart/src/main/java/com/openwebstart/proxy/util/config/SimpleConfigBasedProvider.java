package com.openwebstart.proxy.util.config;

public class SimpleConfigBasedProvider extends AbstractConfigBasedProvider {

    private final ProxyConfiguration proxyConfiguration;

    public SimpleConfigBasedProvider(final ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
    }

    @Override
    protected ProxyConfiguration getConfig() {
        return proxyConfiguration;
    }
}
