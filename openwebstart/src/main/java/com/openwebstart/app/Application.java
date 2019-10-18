package com.openwebstart.app;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.cache.cache.CacheId;

import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Defines a JNLP based application that is manged by OpenWebStart
 */
public class Application {

    private final CacheId cacheId;

    /**
     * Constructor
     * @param cacheId the cache object from IcedTeaWeb
     */
    public Application(final CacheId cacheId) {
        Assert.requireNonNull(cacheId, "cacheId");
        this.cacheId = cacheId;
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
        return cacheId.getFiles().stream().mapToLong(f -> f.getSize()).sum();
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
        return null;
    }
}
