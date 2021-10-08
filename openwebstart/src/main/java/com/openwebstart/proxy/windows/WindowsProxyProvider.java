package com.openwebstart.proxy.windows;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.windows.registry.RegistryQuery;
import com.openwebstart.proxy.windows.registry.RegistryQueryResult;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.Proxy;
import java.net.URI;
import java.util.List;

import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_REGISTRY_KEY;

public class WindowsProxyProvider implements ProxyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsProxyProvider.class);
    private final ProxyProvider internalProvider;

    public WindowsProxyProvider(final DeploymentConfiguration config) {
        this.internalProvider = createProxyProvider(config);
    }

    private ProxyProvider createProxyProvider(DeploymentConfiguration config) {
        try {
            final RegistryQueryResult queryResult = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
            return WindowsProxyUtils.createInternalProxy(config, queryResult);
        } catch (Exception regException) {
            LOG.debug("Falling back to java.net.useSystemProxies as Registry Query Failed: {}", regException.getMessage());
            return WindowsProxyUtils.createSystemProxy();
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
