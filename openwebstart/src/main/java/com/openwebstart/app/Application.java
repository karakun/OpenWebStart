package com.openwebstart.app;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.resources.cache.CacheId;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Defines a JNLP based application that is manged by OpenWebStart
 */
public class Application {

    private final CacheId cacheId;

    private final long size;

    /**
     * Constructor
     * @param cacheId the cache object from IcedTeaWeb
     */
    public Application(final CacheId cacheId) {
        this.cacheId = Assert.requireNonNull(cacheId, "cacheId");
        this.size = cacheId.getFiles().stream().mapToLong(f -> f.getSize()).sum();
    }

    /**
     * Returns the application name
     * @return the name
     */
    public String getName() {
        return cacheId.getId();
    }

    /**
     * returns the current size of the application on disc in bytes
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns an icon that can be used to show the application
     * @param dimension needed size of the icon
     * @return the icon as image
     */
    public BufferedImage getIcon(final int dimension) {
        return null;
    }

    /**
     * Returns the URL of the JNLP file
     * @return
     */
    public URL getJnlpFileUrl() {
        try {
            return Paths.get(cacheId.getId()).toUri().toURL();
        } catch (final Exception e) {
            throw new RuntimeException("Can not get JNLP URL", e);
        }
    }

    public String getId() {
        return cacheId.getId();
    }
}
