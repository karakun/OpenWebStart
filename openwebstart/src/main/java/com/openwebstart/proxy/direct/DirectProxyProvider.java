package com.openwebstart.proxy.direct;

import com.openwebstart.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class DirectProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DirectProxyProvider.class);
    private final static DirectProxyProvider INSTANCE = new DirectProxyProvider();

    private DirectProxyProvider() {
    }

    @Override
    public List<Proxy> select(final URI uri) {
        LOG.debug("Using NO_PROXY");
        return Collections.singletonList(Proxy.NO_PROXY);
    }

    public static DirectProxyProvider getInstance() {
        return INSTANCE;
    }
}
