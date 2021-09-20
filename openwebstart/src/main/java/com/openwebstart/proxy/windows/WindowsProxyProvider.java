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

    public WindowsProxyProvider(final DeploymentConfiguration config) throws Exception {
        RegistryQueryResult queryResult = null;
        try {
            queryResult = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
            LOG.debug("Registry Query Successful");
        } catch (Exception regException) {
            LOG.debug("Will use java.net.useSystemProxies as Registry Query Failed : {}", regException.getMessage());
        }
        internalProvider = queryResult != null ? WindowsProxyUtils.createInternalProxy(config, queryResult) : WindowsProxyUtils.createInternalProxy();
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
