package com.openwebstart.app;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.resources.cache.CacheId;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Defines a JNLP based application that is manged by OpenWebStart
 */
public class Application {

    private final CacheId cacheId;

    private final long size;

    private final JNLPFile jnlpFile;

    /**
     * Constructor
     * @param cacheId the cache object from IcedTeaWeb
     */
    public Application(final CacheId cacheId) throws IOException, ParseException {
        this.cacheId = Assert.requireNonNull(cacheId, "cacheId");
        this.size = cacheId.getFiles().stream().mapToLong(f -> f.getSize()).sum();
        jnlpFile = new JNLPFile(Paths.get(cacheId.getId()).toUri().toURL());
    }

    /**
     * Returns the application name
     * @return the name
     */
    public String getName() {
        try {
            return jnlpFile.getTitle(true);
        } catch (final Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * returns the current size of the application on disc in bytes
     * @return the size
     */
    public long getSize() {
        return size;
    }

    public CompletableFuture<BufferedImage> loadIcon(final int dimension) {
        final CompletableFuture<BufferedImage> result = new CompletableFuture<>();
        final URL iconURL = Optional.ofNullable(jnlpFile.getInformation().getIconLocation(IconKind.SHORTCUT, 64, 64))
                .orElseGet(() -> jnlpFile.getInformation().getIconLocation(IconKind.DEFAULT, 64, 64));
        if(iconURL == null) {
            result.complete(null);
        } else {
            Executors.newSingleThreadExecutor().submit(() -> {
                try(final InputStream inputStream = iconURL.openStream()) {
                    result.complete(ImageIO.read(inputStream));
                } catch (final IOException e) {
                    result.completeExceptionally(e);
                }
            });
        }
        return result;
    }

    /**
     * Returns an icon that can be used to show the application
     * @param dimension needed size of the icon
     * @return the icon as image
     */
    public BufferedImage getIcon(final int dimension) {
        // TODO We do not want to download the icon in the EDT. Maybe this method retunrs Future<BufferedImage>
        // TODO jnlpFile.getInformation().getIcons()
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

    public JNLPFile getJnlpFile() {
        return jnlpFile;
    }

    public String getId() {
        return cacheId.getId();
    }
}
