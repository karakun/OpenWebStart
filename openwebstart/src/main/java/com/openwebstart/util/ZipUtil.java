package com.openwebstart.util;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    public static void unzip(final InputStream zippedInputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(zippedInputStream, "zippedInputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        try (final ZipInputStream zipInputStream = new ZipInputStream(zippedInputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                final String fileName = zipEntry.getName();
                final Path newFile = baseDir.resolve(fileName);
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    try (final OutputStream outputStream = Files.newOutputStream(newFile)) {
                        IOUtils.copy(zipInputStream, outputStream);
                    }
                    newFile.toFile().setExecutable(true);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

}
