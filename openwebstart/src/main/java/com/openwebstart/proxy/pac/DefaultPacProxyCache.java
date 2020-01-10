package com.openwebstart.proxy.pac;

import net.sourceforge.jnlp.util.TimedHashMap;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultPacProxyCache implements PacProxyCache {

    private final TimedHashMap<String, String> pacCache = new TimedHashMap<>();

    private final Lock cacheLock = new ReentrantLock();

    private int getPort(final URI uri) {
        final int port = uri.getPort();
        if (port == -1) {
            try {
                return uri.toURL().getDefaultPort();
            } catch (final Exception ignore) {
            }
        }
        return port;
    }

    private String createLookupString(final URI uri) {
        return uri.getScheme() + "://" + uri.getHost() + ":" + getPort(uri);
    }

    /**
     * Gets an entry from the cache
     */
    public String getFromCache(final URI uri) {
        final String lookupString = createLookupString(uri);
        cacheLock.lock();
        try {
            return pacCache.get(lookupString);
        } finally {
            cacheLock.unlock();
        }
    }

    /**
     * Adds an entry to the cache
     */
    public void addToCache(final URI uri, final String proxyResult) {
        final String lookupString = createLookupString(uri);
        cacheLock.lock();
        try {
            pacCache.put(lookupString, proxyResult);
        } finally {
            cacheLock.unlock();
        }
    }
}
