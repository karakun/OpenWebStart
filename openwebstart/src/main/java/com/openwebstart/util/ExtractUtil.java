package com.openwebstart.util;

import com.openwebstart.func.Result;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

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

        ArchiveEntry entry = inputStream.getNextEntry();
        while (entry != null) {
            storeFileOnDisc(inputStream, baseDir, entry);
            entry = inputStream.getNextEntry();
        }
        unwrapPossibleSubDirectories(baseDir, baseDir);
    }

    /**
     * Sometimes an archive contains a folder as root entry and all content is part of this folder.
     * This method unwraps such folder and moves the complete content 1 level up.
     * The method support several folders that are wrapped in each other.
     * This method is only visible for testing.
     *
     * @param srcDir
     * @param targetDir
     * @return
     */
    static boolean unwrapPossibleSubDirectories(final Path srcDir, final Path targetDir) {
        final List<File> directChildren = listFiles(srcDir);
        if (directChildren.size() == 1) {
            LOG.debug("Only 1 file extracted...");
            final File onlyChild = directChildren.get(0);
            //let's check if we extracted everything in an internal directory
            boolean wrappedDir = onlyChild.isDirectory();
            if (wrappedDir) {
                final File folder = new File(onlyChild.getParent(), UUID.randomUUID().toString());
                onlyChild.renameTo(folder);

                if (!unwrapPossibleSubDirectories(folder.toPath(), targetDir)) {
                    //let's move the complete content 1 level up
                    final Path directoryPath = srcDir.resolve(folder.toPath());
                    LOG.debug("Will unwrap extracted folder {}", directoryPath);
                    listFiles(directoryPath).stream()
                            .map(Result.of(f -> moveToDirAndReplace(targetDir, f)))
                            .filter(Result::isFailed)
                            .findFirst()
                            .ifPresent(r -> {
                                throw new RuntimeException("Error in unwrapping extracted directory!", r.getException());
                            });
                }
                FileUtils.deleteWithErrMesg(folder);
                return true;
            }
        }
        return false;

    }

    private static List<File> listFiles(Path baseDir) {
        final File[] files = baseDir.toFile().listFiles();
        return files != null ? Arrays.asList(files) : emptyList();
    }

    private static Path moveToDirAndReplace(final Path baseDir, final File toMove) throws IOException {
        final Path p = toMove.toPath();
        return Files.move(p, baseDir.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
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
            newFile.toFile().setExecutable(true);
        }
    }
}
