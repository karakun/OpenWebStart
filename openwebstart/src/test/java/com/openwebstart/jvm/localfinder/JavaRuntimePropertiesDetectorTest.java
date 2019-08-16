package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.localfinder.JavaRuntimePropertiesDetector.JavaRuntimeProperties;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_HOME;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VENDOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JavaRuntimePropertiesDetectorTest {

    @Test
    void testExtractionOfRuntime() throws Exception {
        final String javaHome = System.getProperty(JAVA_HOME);
        final Path home = FileSystems.getDefault().getPath(javaHome);
        final Stream<Path> files = Files.find(home, 5, this::isJavaExecutable, FileVisitOption.FOLLOW_LINKS);
        final Path javaExecutable = files.findFirst().orElseThrow(() -> new AssertionFailedError("could not find java executable"));
        final JavaRuntimeProperties result = JavaRuntimePropertiesDetector.getProperties(javaExecutable);

        assertNotNull(result);
        assertEquals(System.getProperty(JAVA_VENDOR), result.getVendor());
        assertEquals(System.getProperty(JAVA_VERSION), result.getVersion());
        assertEquals(System.getProperty(OS_NAME), result.getOsName());
        assertEquals(System.getProperty(OS_ARCH), result.getOsArch());
    }

    private boolean isJavaExecutable(Path path, BasicFileAttributes basicFileAttributes) {
        final String fileName = path.getFileName().toString();
        return fileName.equalsIgnoreCase("java") || fileName.equalsIgnoreCase("java.exe");
    }
}
