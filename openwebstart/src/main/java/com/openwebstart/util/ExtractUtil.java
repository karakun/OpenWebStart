package com.openwebstart.util;


import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractUtil {

    public static void unZip(final InputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        try (final ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                storeFileOnDisc(zipInputStream, baseDir, entry.isDirectory(), entry.getName());
                entry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

    public static void unTar(final InputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        try (final TarArchiveInputStream tarInputstream = new TarArchiveInputStream(inputStream)) {
            ArchiveEntry entry = tarInputstream.getNextEntry();
            while (entry != null) {
                storeFileOnDisc(tarInputstream, baseDir, entry.isDirectory(), entry.getName());
                entry = tarInputstream.getNextEntry();
            }
        }
    }

    public static void unTarGzip(final InputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        try (final GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(inputStream)) {
            unTar(gzipInputStream, baseDir);
        }
    }

    private static void storeFileOnDisc(final InputStream inputStream, final Path baseDir, final boolean isDirectory, final String fileName) throws IOException {
        final Path newFile = baseDir.resolve(fileName);
        if (isDirectory) {
            Files.createDirectories(newFile);
        } else {
            Files.createDirectories(newFile.getParent());
            try (final OutputStream outputStream = Files.newOutputStream(newFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            newFile.toFile().setExecutable(true);
        }
    }
}
