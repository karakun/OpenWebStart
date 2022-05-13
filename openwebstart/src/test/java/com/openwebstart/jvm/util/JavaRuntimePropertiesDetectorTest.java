package com.openwebstart.jvm.util;

import com.openwebstart.jvm.util.JavaRuntimePropertiesDetector.JavaRuntimeProperties;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static com.openwebstart.jvm.os.OperationSystem.OS_BITNESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JavaRuntimePropertiesDetectorTest {

    @Test
    void testExtractionOfRuntime() {
        final String javaHome = JavaSystemProperties.getJavaHome();
        final Path home = FileSystems.getDefault().getPath(javaHome);
        final JavaRuntimeProperties result = JavaRuntimePropertiesDetector.getProperties(home);

        assertNotNull(result);
        assertEquals(JavaSystemProperties.getJavaVendor(), result.getVendor());
        assertEquals(JavaSystemProperties.getJavaVersion(), result.getVersion());
        assertEquals(JavaSystemProperties.getOsName(), result.getOsName());
        assertEquals(JavaSystemProperties.getOsArch(), result.getOsArch());
        assertEquals(System.getProperty(OS_BITNESS), result.getBitness());
    }
}
