package com.openwebstart.app.icon;

import com.openwebstart.app.Application;
import net.adoptopenjdk.icedteaweb.Assert;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ApplicationIconCache {

    private final Map<IconDescription, BufferedImage> cache = new HashMap<>();

    private final Map<IconDescription, CompletableFuture<BufferedImage>> inProgressMap = new HashMap<>();

    private final Lock inProgressMapLock = new ReentrantLock();

    private final Lock cacheLock = new ReentrantLock();

    private final Executor executor;

    public ApplicationIconCache(final Executor executor) {
        this.executor = Assert.requireNonNull(executor, "executor");
    }

    public synchronized Optional<BufferedImage> get(final Application application, final IconDimensions dimension) {
        final IconDescription description = new IconDescription(application.getId(), dimension);
        cacheLock.lock();
        try {
            return Optional.ofNullable(cache.get(description));
        } finally {
            cacheLock.unlock();
        }
    }

    public synchronized CompletableFuture<BufferedImage> getOrLoadIcon(final Application application, final IconDimensions dimension) {
        final Optional<BufferedImage> fromCache = get(application, dimension);
        if (fromCache.isPresent()) {
            return CompletableFuture.completedFuture(fromCache.get());
        } else {
            return loadAndAdd(application, dimension);
        }
    }

    public synchronized void triggerDownload(final Application application, final IconDimensions dimension, final boolean reloadIfAlreadyInCache) {
        if (reloadIfAlreadyInCache || !isInCache(application, dimension)) {
            executor.execute(() -> loadAndAdd(application, dimension));
        }
    }

    private synchronized CompletableFuture<BufferedImage> loadAndAdd(final Application application, final IconDimensions dimension) {
        inProgressMapLock.lock();
        try {
            final IconDescription iconDescription = new IconDescription(application.getId(), dimension);
            if(inProgressMap.containsKey(iconDescription)) {
                return inProgressMap.get(iconDescription);
            } else {
                final CompletableFuture<BufferedImage> result = new CompletableFuture<>();
                inProgressMap.put(iconDescription, result);
                executor.execute(() -> {
                    try {
                        final BufferedImage icon = ApplicationIconDownloadUtils.downloadIcon(application, dimension);
                        cacheLock.lock();
                        try {
                            cache.put(iconDescription, icon);
                        } finally {
                            cacheLock.unlock();
                        }
                        result.complete(icon);
                    } catch (final Exception e) {
                        result.completeExceptionally(e);
                    } finally {
                        inProgressMapLock.lock();
                        try {
                            inProgressMap.remove(iconDescription);
                        } finally {
                            inProgressMapLock.unlock();
                        }
                    }
                });
                return result;
            }
        } finally {
            inProgressMapLock.unlock();
        }
    }

    private synchronized boolean isInCache(final Application application, final IconDimensions dimension) {
        return get(application, dimension).isPresent();
    }

    private class IconDescription {

        final String appId;

        final IconDimensions dimension;

        public IconDescription(final String appId, final IconDimensions dimension) {
            this.appId = appId;
            this.dimension = dimension;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final IconDescription that = (IconDescription) o;
            return Objects.equals(appId, that.appId) &&
                    dimension == that.dimension;
        }

        @Override
        public int hashCode() {
            return Objects.hash(appId, dimension);
        }

        public String getAppId() {
            return appId;
        }

        public IconDimensions getDimension() {
            return dimension;
        }
    }
}
