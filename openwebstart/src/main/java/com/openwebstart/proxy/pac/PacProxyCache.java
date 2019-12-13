package com.openwebstart.proxy.pac;

import net.sourceforge.jnlp.util.TimedHashMap;

import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PacProxyCache {

    private final TimedHashMap<String, String> pacCache = new TimedHashMap<>();

    private final Lock cacheLock = new ReentrantLock();

    /**
     * Gets an entry from the cache
     */
    public String getFromCache(final URL url) {
        final String lookupString = url.getProtocol() + "://" + url.getHost();
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
    public void addToCache(final URL url, final String proxyResult) {
        final String lookupString = url.getAuthority() + "://" + url.getHost();
        cacheLock.lock();
        try {
            pacCache.put(lookupString, proxyResult);
        } finally {
            cacheLock.unlock();
        }
    }
}
