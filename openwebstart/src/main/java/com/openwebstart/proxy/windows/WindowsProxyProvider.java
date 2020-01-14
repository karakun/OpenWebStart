package com.openwebstart.proxy.windows;

import com.openwebstart.proxy.ProxyProvider;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_REGISTRY_KEY;

public class WindowsProxyProvider implements ProxyProvider {

    private final ProxyProvider internalProvider;

    public WindowsProxyProvider(final DeploymentConfiguration config) throws Exception {
        final Map<String, RegistryValue> proxyRegistryEntries = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
        internalProvider = WindowsProxyUtils.createInternalProxy(config, proxyRegistryEntries);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
