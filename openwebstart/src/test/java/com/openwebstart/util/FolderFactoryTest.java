package com.openwebstart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class FolderFactoryTest {

    @ParameterizedTest
    @MethodSource("getTestData")
    public void createSimpleFolder(final boolean simplifyName, final String data, final String expectedName) throws IOException {
        //given
        final FolderFactory folderFactory = new FolderFactory(getBasePathForTests(), simplifyName);
        final String folderName = data;

        //when
        final Path createdFolder = folderFactory.createSubFolder(folderName);

        //then
        try {
            Assertions.assertEquals(expectedName, createdFolder.toFile().getName());
            Assertions.assertTrue(createdFolder.toFile().exists());
            Assertions.assertTrue(createdFolder.toFile().isDirectory());
            Assertions.assertEquals(getBasePathForTests().toFile(), createdFolder.toFile().getParentFile());
        } finally {
            Files.delete(createdFolder);
        }
    }

    private static Stream<Arguments> getTestData() {
        return Stream.of(Arguments.of(true, "a b", "a-b"),
                Arguments.of(true, "Hello", "hello"),
                Arguments.of(true, "folder-test-aaa", "folder-test-aaa-2"),
                Arguments.of(true, "folder-test-bbb", "folder-test-bbb-1"),
                Arguments.of(true, "folder-test-ccc", "folder-test-ccc-4"),
                Arguments.of(true, "folder-test-ddd", "folder-test-ddd-1"),
                Arguments.of(true, "folder-test-eee", "folder-test-eee"),
                Arguments.of(true, "folder test eee", "folder-test-eee"),
                Arguments.of(true, "folder       test      eee", "folder-test-eee"),
                Arguments.of(true, "folder-test-aaa-1", "folder-test-aaa-1-1"));
    }

    private static Path getBasePathForTests() {
        final File resource = new File(FolderFactoryTest.class.getResource("folder-test-aaa").getFile());
        return resource.getParentFile().toPath();
    }
}