package com.openwebstart.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link ExtractUtil}.
 */
class ExtractUtilTest {

    @Test
    void unwrapFromNoSubDir(@TempDir final File cacheDir) throws IOException {
        // given
        final File extractDir = createDir(cacheDir, UUID.randomUUID().toString());
        createJvmFilesIn(extractDir);

        // when
        ExtractUtil.moveJavaHomeToTarget(extractDir.toPath(), cacheDir.toPath());

        // then
        assertJvmFilesIn(cacheDir);
    }

    @Test
    void unwrapFromSingleSubDir(@TempDir final File cacheDir) throws IOException {
        // given
        final File extractDir = createDir(cacheDir, UUID.randomUUID().toString());
        final File subDir = createDir(extractDir, "v1.8_242");
        createJvmFilesIn(subDir);

        // when
        ExtractUtil.moveJavaHomeToTarget(extractDir.toPath(), cacheDir.toPath());

        // then
        assertJvmFilesIn(cacheDir);
    }

    @Test
    void unwrapFromTwoSubDir(@TempDir final File cacheDir) throws IOException {
        // given
        final File extractDir = createDir(cacheDir, UUID.randomUUID().toString());
        final File subDir = createDir(createDir(extractDir, "azul"), "v1.8_242");
        createJvmFilesIn(subDir);

        // when
        ExtractUtil.moveJavaHomeToTarget(extractDir.toPath(), cacheDir.toPath());

        // then
        assertJvmFilesIn(cacheDir);
    }

    @Test
    void unwrapFromOddlyNamedSingleSubDir(@TempDir final File cacheDir) throws IOException {
        // given
        final File extractDir = createDir(cacheDir, UUID.randomUUID().toString());
        final File subDir = createDir(extractDir, "bin");
        createJvmFilesIn(subDir);

        // when
        ExtractUtil.moveJavaHomeToTarget(extractDir.toPath(), cacheDir.toPath());

        // then
        assertJvmFilesIn(cacheDir);
    }

    @Test
    void unwrapFromSubDirWithReadme(@TempDir final File cacheDir) throws IOException {
        // given
        final File extractDir = createDir(cacheDir, UUID.randomUUID().toString());
        final File subDir = createDir(extractDir, "v1.8_242");
        createFile(extractDir, "README.txt");
        createJvmFilesIn(subDir);

        // when
        ExtractUtil.moveJavaHomeToTarget(extractDir.toPath(), cacheDir.toPath());

        // then
        assertJvmFilesIn(cacheDir);
    }

    private void createJvmFilesIn(final File dir) throws IOException {
        final File bin = createDir(dir, "bin");
        createFile(bin, "java");
        createFile(bin, "javac");

        final File lib = createDir(dir, "lib");
        createFile(lib, "jconsole.jar");
        createFile(lib, "tools.jar");

        final File amd64 = createDir(lib, "amd64");
        createFile(amd64, "libjawt.so");

        createFile(dir, "ASSEMBLY_EXCEPTION");
        createFile(dir, "LICENSE");
        createFile(dir, "readme.txt");
        createFile(dir, "release");
        createFile(dir, "src.zip");
        createFile(dir, "THIRD_PARTY_README");
    }

    private void assertJvmFilesIn(final File dir) {
        assertTrue(dir.isDirectory(), "target dir is not a directory " + dir);

        final File bin = new File(dir, "bin");
        final File lib = new File(dir, "lib");
        final File amd64 = new File(lib, "amd64");

        assertTrue(bin.isDirectory(), "cannot find bin directory");
        assertTrue(new File(bin, "java").isFile(), "cannot find bin/java");
        assertTrue(new File(bin, "javac").isFile(), "cannot find bin/javac");

        assertTrue(lib.isDirectory(), "cannot find lib directory");
        assertTrue(new File(lib, "jconsole.jar").isFile(), "cannot find lib/jconsole.jar");
        assertTrue(new File(lib, "tools.jar").isFile(), "cannot find lib/tools.jar");

        assertTrue(amd64.isDirectory(), "cannot find lib/amd64 directory");
        assertTrue(new File(amd64, "libjawt.so").isFile(), "cannot find lib/amd64/libjawt.so");

        assertTrue(new File(dir, "ASSEMBLY_EXCEPTION").isFile(), "cannot find ASSEMBLY_EXCEPTION");
        assertTrue(new File(dir, "LICENSE").isFile(), "cannot find LICENSE");
        assertTrue(new File(dir, "readme.txt").isFile(), "cannot find readme.txt");
        assertTrue(new File(dir, "release").isFile(), "cannot find release");
        assertTrue(new File(dir, "src.zip").isFile(), "cannot find src.zip");
        assertTrue(new File(dir, "THIRD_PARTY_README").isFile(), "cannot find THIRD_PARTY_README");
    }

    private File createDir(final File parent, final String name) {
        final File result = new File(parent, name);
        assertTrue(result.mkdirs(), "Failed to create directory " + result);
        return result;
    }

    private void createFile(final File parent, final String name) throws IOException {
        final File result = new File(parent, name);
        assertTrue(result.createNewFile(), "Failed to create file " + result);
        assertTrue(result.setExecutable(true));
    }
}
