package com.openwebstart.app.icon;

import com.openwebstart.app.Application;
import com.openwebstart.http.DownloadInputStream;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApplicationIconHandler {

    private final static Logger LOG = LoggerFactory.getLogger(DownloadInputStream.class);

    private final static ApplicationIconHandler INSTANCE = new ApplicationIconHandler();

    private final ApplicationIconCache applicationIconCache;

    private final BufferedImage defaultIcon32;

    private final BufferedImage defaultIcon64;

    private final BufferedImage defaultIcon128;

    private final BufferedImage defaultIcon256;

    private final BufferedImage defaultIcon512;

    public ApplicationIconHandler() {
        this.applicationIconCache = new ApplicationIconCache(Executors.newCachedThreadPool());
        defaultIcon32 = createDefaultIcon(IconDimensions.SIZE_32);
        defaultIcon64 = createDefaultIcon(IconDimensions.SIZE_64);
        defaultIcon128 = createDefaultIcon(IconDimensions.SIZE_128);
        defaultIcon256 = createDefaultIcon(IconDimensions.SIZE_256);
        defaultIcon512 = createDefaultIcon(IconDimensions.SIZE_512);
    }

    public BufferedImage getIconOrDefault(final Application application, final IconDimensions dimension) {
        final Optional<BufferedImage> image = applicationIconCache.get(application, dimension);
        if (!image.isPresent()) {
            applicationIconCache.triggerDownload(application, dimension, false);
        }
        return image.orElseGet(() -> getDefaultIcon(dimension));
    }

    public Future<BufferedImage> getIcon(final Application application, final IconDimensions dimension) {
        Assert.requireNonNull(application, "application");
        Assert.requireNonNull(dimension, "dimension");

        final Optional<BufferedImage> image = applicationIconCache.get(application, dimension);
        if (image.isPresent()) {
            return CompletableFuture.completedFuture(image.get());
        } else {
            return applicationIconCache.getOrLoadIcon(application, dimension).handle((i, e) -> {
                if (e != null) {
                    return getDefaultIcon(dimension);
                } else {
                    return Optional.ofNullable(i).orElse(getDefaultIcon(dimension));
                }
            });
        }
    }

    private BufferedImage getDefaultIcon(final IconDimensions dimension) {
        if (dimension == IconDimensions.SIZE_32) {
            return defaultIcon32;
        } else if (dimension == IconDimensions.SIZE_64) {
            return defaultIcon64;
        } else if (dimension == IconDimensions.SIZE_128) {
            return defaultIcon128;
        } else if (dimension == IconDimensions.SIZE_256) {
            return defaultIcon256;
        } else if (dimension == IconDimensions.SIZE_512) {
            return defaultIcon512;
        }
        throw new IllegalArgumentException("No default icon defined for dimension " + dimension);
    }

    private static BufferedImage createDefaultIcon(final IconDimensions dimension) {
        Assert.requireNonNull(dimension, "dimension");
        try {
            return ImageIO.read(ApplicationIconHandler.class.getResource("default-icon-" + dimension.getDimension() + ".png"));
        } catch (Exception e) {
            LOG.error("Unable to load default icon for dimension " + dimension, e);
            return new BufferedImage(dimension.getDimension(), dimension.getDimension(), BufferedImage.TYPE_INT_ARGB);
        }
    }

    public static ApplicationIconHandler getInstance() {
        return INSTANCE;
    }
}
