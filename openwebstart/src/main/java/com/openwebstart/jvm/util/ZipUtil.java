package com.openwebstart.jvm.util;


import net.adoptopenjdk.icedteaweb.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    public static void unzip(final InputStream zippedInputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(zippedInputStream, "zippedInputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        try (final ZipInputStream zipInputStream = new ZipInputStream(zippedInputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                final String fileName = zipEntry.getName();
                final Path newFile = Paths.get(baseDir.toString(), fileName);
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    final File parentFile = newFile.toFile().getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    try (final OutputStream outputStream = Files.newOutputStream(newFile)) {
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, len);
                        }
                    }
                    newFile.toFile().setExecutable(true);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

}
