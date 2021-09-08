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
        ProxyProvider temp = null;
        Exception ex = null;
        try {
            final RegistryQueryResult queryResult = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
            temp = WindowsProxyUtils.createInternalProxy(config, queryResult);
            LOG.debug("**** Registry Query Successful");
        } catch (Exception e) {
            ex = e;
            LOG.debug("**** Registry Query Failed. Using proxy from java.net.useSystemProxies");
            temp = WindowsProxyUtils.createInternalProxy();
        }
        internalProvider = temp;
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
