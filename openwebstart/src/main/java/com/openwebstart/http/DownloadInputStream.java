package com.openwebstart.http;

import com.openwebstart.util.Subscription;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DownloadInputStream extends InputStream {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadInputStream.class);
    public static final String SHA_256 = "SHA-256";

    private final List<Consumer<Double>> downloadPercentageListeners;

    private final List<Consumer<Long>> downloadDoneListeners;

    private final List<Consumer<Exception>> onErrorListeners;

    private final DigestInputStream wrappedStream;

    private final long dataSize;
    private final URL connectionUrl;

    private final AtomicLong updateChunkSize;

    private final AtomicLong downloaded;

    private final AtomicLong lastUpdateSize;

    private final AtomicBoolean firstRead;

    private final DownloadType downloadType;

    public DownloadInputStream(HttpResponse response) throws IOException {
        this(response.getContentStream(), response.getContentSize(), response.getConnectionUrl());
    }

    public DownloadInputStream(final InputStream inputStream, final long dataSize, URL connectionUrl) {
        this.dataSize = dataSize > 0 ? dataSize : -1;
        this.connectionUrl = connectionUrl;
        if (dataSize > 0) {
            downloadType = DownloadType.NORMAL;
        } else {
            downloadType = DownloadType.INDETERMINATE;
        }
        this.downloaded = new AtomicLong(0);
        this.lastUpdateSize = new AtomicLong(0);
        this.firstRead = new AtomicBoolean(true);
        this.downloadPercentageListeners = new CopyOnWriteArrayList<>();
        this.downloadDoneListeners = new CopyOnWriteArrayList<>();
        this.onErrorListeners = new CopyOnWriteArrayList<>();
        this.updateChunkSize = new AtomicLong(1000);
        if (dataSize > 0) {
            this.updateChunkSize.set(dataSize / 1000);
        }

        try {
            this.wrappedStream = ConnectionUtils.createHashStream(inputStream, SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No HASH_ALGORITHM support");
        }
    }

    public void setUpdateChunkSize(final long updateChunkSize) {
        if (updateChunkSize <= 0) {
            throw new IllegalArgumentException("chunk size must be > 0");
        }
        this.updateChunkSize.set(updateChunkSize);
    }

    public CompletableFuture<String> getHash() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        addDownloadDoneListener(size ->
        {
            future.complete(ConnectionUtils.toHex(wrappedStream.getMessageDigest().digest()));
            try {
                LOG.debug("Done Download checksum {} from {}", future.get(), connectionUrl);
            } catch (Exception e) {
                LOG.debug("Could not get Download checksum {}", e.getMessage());
            }
        });
        return future;
    }

    public DownloadType getDownloadType() {
        return downloadType;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        try {
            final int count = super.read(b, off, len);
            if (count < 0) {
                onDone();
            }
            return count;
        } catch (final Exception e) {
            try {
                onError(e);
            } finally {
                throw e;
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (final Exception e) {
            try {
                onError(e);
            } finally {
                throw e;
            }
        }
        onDone();
    }

    public int read() throws IOException {
        try {
            if (firstRead.get()) {
                onStart();
                firstRead.set(false);
            }
            final int value = wrappedStream.read();
            if (value >= 0) {
                update(1);
            }
            return value;
        } catch (final Exception e) {
            try {
                onError(e);
            } finally {
                throw e;
            }
        }
    }

    @Override
    public int available() throws IOException {
        try {
            final int available = super.available();
            if (available < 0) {
                onDone();
            }
            return available;
        } catch (final Exception e) {
            try {
                onError(e);
            } finally {
                throw e;
            }
        }
    }

    public Subscription addDownloadPercentageListener(final Consumer<Double> listener) {
        Assert.requireNonNull(listener, "listener");
        downloadPercentageListeners.add(listener);
        return () -> downloadPercentageListeners.remove(listener);
    }

    public Subscription addDownloadDoneListener(final Consumer<Long> listener) {
        Assert.requireNonNull(listener, "listener");
        downloadDoneListeners.add(listener);
        return () -> downloadDoneListeners.remove(listener);
    }

    public Subscription addDownloadErrorListener(final Consumer<Exception> listener) {
        Assert.requireNonNull(listener, "listener");
        onErrorListeners.add(listener);
        return () -> onErrorListeners.remove(listener);
    }

    private void onDone() {
        LOG.debug("Done Download of size {} from {}", downloaded.get(), connectionUrl);
        downloadDoneListeners.forEach(l -> l.accept(dataSize));
    }

    private void onStart() {
        getHash();
        LOG.debug("Download of size {} started from {}", dataSize, connectionUrl);
    }

    private void onError(final Exception e) {
        LOG.error("Error {} while downloading URL {}",e.getMessage(), connectionUrl);
        onErrorListeners.forEach(l -> l.accept(e));
    }

    private synchronized void update(final int len) {
        final long currentSize = downloaded.addAndGet(len);
        if (lastUpdateSize.get() + updateChunkSize.get() <= currentSize) {
            lastUpdateSize.set(currentSize);
            if (Objects.equals(downloadType, DownloadType.NORMAL)) {
                final double percentageDone = (((double) currentSize) / ((double) dataSize / 100.0)) / 100.0;
                downloadPercentageListeners.forEach(l -> l.accept(percentageDone));
            } else {
                downloadPercentageListeners.forEach(l -> l.accept(-1d));
            }
        }
    }

    public long getDownloaded() {
        return downloaded.get();
    }

    public long getDataSize() {
        return dataSize;
    }
}
