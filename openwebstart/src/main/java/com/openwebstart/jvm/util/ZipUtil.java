package com.openwebstart.jvm.util;


import net.adoptopenjdk.icedteaweb.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    private final static int DEFAULT_BUFFER_SIZE = 1024;

    public static void unzip(final InputStream zippedInputStream, final Path baseDir) throws IOException {
        unzip(zippedInputStream, baseDir, DEFAULT_BUFFER_SIZE);
    }

    public static void unzip(final InputStream zippedInputStream, final Path baseDir, int bufferSize) throws IOException {
        unzip(zippedInputStream, baseDir, bufferSize, p -> {});
    }

    public static void unzip(final InputStream zippedInputStream, final Path baseDir, Consumer<Path> fileConsumer) throws IOException {
        unzip(zippedInputStream, baseDir, DEFAULT_BUFFER_SIZE, fileConsumer);
    }

    public static void unzip(final InputStream zippedInputStream, final Path baseDir, int bufferSize, final Consumer<Path> fileConsumer) throws IOException {
        Assert.requireNonNull(zippedInputStream, "zippedInputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        Assert.requireNonNull(fileConsumer, "fileConsumer");
        try(final ZipInputStream zipInputStream = new ZipInputStream(zippedInputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                final byte[] buffer = new byte[bufferSize];
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
                    fileConsumer.accept(newFile);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

}
