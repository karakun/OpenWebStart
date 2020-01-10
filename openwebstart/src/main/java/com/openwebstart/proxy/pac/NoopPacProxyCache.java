package com.openwebstart.proxy.pac;

import java.net.URI;

public class NoopPacProxyCache implements PacProxyCache {

    @Override
    public String getFromCache(final URI uri) {
        return null;
    }

    @Override
    public void addToCache(final URI uri, final String proxyResult) {

    }
}
