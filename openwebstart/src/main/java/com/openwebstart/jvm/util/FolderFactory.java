package com.openwebstart.jvm.util;

import net.adoptopenjdk.icedteaweb.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FolderFactory {

    private final Path basePath;

    private final Lock subfolderLock = new ReentrantLock();

    public FolderFactory(final Path basePath) {
        this.basePath = Assert.requireNonNull(basePath, "basePath");
    }

    public Path createSubFolder(final String name) {
        Assert.requireNonBlank(name, "name");
        subfolderLock.lock();
        try {
            final Path subfolderPath = Paths.get(basePath.toString(), name);
            if(subfolderPath.toFile().exists()) {
                return createSubFolder(name, 1);
            } else {
                subfolderPath.toFile().mkdirs();
                return subfolderPath;
            }
        } finally {
            subfolderLock.unlock();
        }
    }

    private Path createSubFolder(final String name, final long suffix) {
        Assert.requireNonBlank(name, "name");
        subfolderLock.lock();
        try {
            final String foldername = name + "-" + suffix;
            final Path subfolderPath = Paths.get(basePath.toString(), foldername);
            if(subfolderPath.toFile().exists()) {
                return createSubFolder(name, suffix + 1);
            } else {
                subfolderPath.toFile().mkdirs();
                return subfolderPath;
            }
        } finally {
            subfolderLock.unlock();
        }
    }
}
