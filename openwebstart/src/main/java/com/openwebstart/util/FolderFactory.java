package com.openwebstart.util;

import net.adoptopenjdk.icedteaweb.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FolderFactory {

    private final Path basePath;

    private final Lock subfolderLock = new ReentrantLock();

    private final boolean simplifyName;

    public FolderFactory(final Path basePath, boolean simplifyName) {
        this.basePath = Assert.requireNonNull(basePath, "basePath");
        this.simplifyName = simplifyName;
    }

    public Path createSubFolder(final String name) {
        final String folderName = simplifyName ? FilenameUtil.toSimplifiedFileName(name) : name;
        return createSubFolder(folderName, 0, false);
    }

    private Path createSubFolder(final String name, final int suffix, boolean addSuffix) {
        Assert.requireNonBlank(name, "name");

        final String foldername = addSuffix ? name + "-" + suffix : name;
        final Path subfolderPath = Paths.get(basePath.toString(), foldername);

        subfolderLock.lock();
        try {
            if (subfolderPath.toFile().exists()) {
                return createSubFolder(name, suffix + 1, true);
            } else {
                if (subfolderPath.toFile().mkdirs()) {
                    return subfolderPath;
                } else {
                    throw new RuntimeException("unable to create folder '" + subfolderPath + "'");
                }
            }
        } finally {
            subfolderLock.unlock();
        }
    }
}
