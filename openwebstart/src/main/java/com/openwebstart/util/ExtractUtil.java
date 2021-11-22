package com.openwebstart.util;

import com.openwebstart.jvm.localfinder.JdkFinder;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;

import static java.nio.file.FileVisitResult.CONTINUE;

public class ExtractUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractUtil.class);

    public static void unZip(final InputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        try (final ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream)) {
            extract(zipInputStream, baseDir);
        }
    }

    public static void unTar(final InputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        try (final TarArchiveInputStream tarInputstream = new TarArchiveInputStream(inputStream)) {
            extract(tarInputstream, baseDir);
        }
    }

    public static void unTarGzip(final InputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        try (final GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(inputStream)) {
            unTar(gzipInputStream, baseDir);
        }
    }

    private static void extract(final ArchiveInputStream inputStream, final Path baseDir) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(baseDir, "baseDir");

        final File tempDir = new File(baseDir.toFile(), UUID.randomUUID().toString());
        if (!tempDir.mkdirs()) {
            throw new RuntimeException("could not create temp dir " + tempDir);
        }

        try {
            ArchiveEntry entry = inputStream.getNextEntry();
            while (entry != null) {
                storeFileOnDisc(inputStream, tempDir.toPath(), entry);
                entry = inputStream.getNextEntry();
            }
            moveJavaHomeToTarget(tempDir.toPath(), baseDir);
        } finally {
            FileUtils.recursiveDelete(tempDir, tempDir);
        }
    }

    /**
     * Finds a Java home within the search directory and moves it to the target directory.
     * This is required as some vendors do have a subdirectory structure in their archives.
     */
    static void moveJavaHomeToTarget(final Path searchRoot, final Path targetDir) {
        if (Files.isDirectory(searchRoot)) {
            try {
                final Path javaHome = Files.find(searchRoot, 5, JdkFinder::isJavaHome)
                        .map(Path::toAbsolutePath)
                        .map(Path::normalize)
                        .min(Comparator.comparingInt(Path::getNameCount))
                        .orElseThrow(() -> new IllegalStateException("Java not found in " + searchRoot));

                moveFiles(targetDir, javaHome);
            } catch (final IOException e) {
                throw new RuntimeException("Error while searching for local JVMs at '" + searchRoot + "'", e);
            }
        }
    }

    private static void moveFiles(final Path targetDir, final Path javaHome) throws IOException {
        Files.walkFileTree(javaHome, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        final Path currentTargetDir = targetDir.resolve(javaHome.relativize(dir));
                        try {
                            Files.copy(dir, currentTargetDir);
                        } catch (FileAlreadyExistsException e) {
                            if (!Files.isDirectory(currentTargetDir)) {
                                throw e;
                            }
                        }
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.move(file, targetDir.resolve(javaHome.relativize(file)));
                        return CONTINUE;
                    }
                });
    }

    private static void storeFileOnDisc(final InputStream inputStream, final Path baseDir, final ArchiveEntry entry) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(baseDir, "baseDir");
        Assert.requireNonNull(entry, "entry");

        final Path newFile = baseDir.resolve(entry.getName());
        if (entry.isDirectory()) {
            Files.createDirectories(newFile);
        } else {
            Files.createDirectories(newFile.getParent());
            try (final OutputStream outputStream = Files.newOutputStream(newFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            if (!newFile.toFile().setExecutable(true)) {
                LOG.warn("failed to set executable flag on file {}", newFile);
            }
        }
    }
}
