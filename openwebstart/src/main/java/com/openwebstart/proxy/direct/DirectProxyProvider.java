package com.openwebstart.proxy.direct;

import com.openwebstart.proxy.ProxyProvider;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class DirectProxyProvider implements ProxyProvider {

    private final static DirectProxyProvider INSTANCE = new DirectProxyProvider();

    private DirectProxyProvider() {
    }

    @Override
    public List<Proxy> select(final URI uri) {
        return Collections.singletonList(Proxy.NO_PROXY);
    }

    public static DirectProxyProvider getInstance() {
        return INSTANCE;
    }
}
