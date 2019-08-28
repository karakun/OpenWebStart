package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.util.JavaRuntimePropertiesDetector;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

public class JdkFinder {
    private static final Logger LOG = LoggerFactory.getLogger(JdkFinder.class);

    public static final OperationSystem LOCAL_OS = OperationSystem.getLocalSystem();


    public static List<Result<LocalJavaRuntime>> findLocalJdks(final Path... searchRoots) {
        LOG.debug("About to look for local jdks at the following locations: {}",
                Arrays.stream(searchRoots).map(path -> path.normalize().toAbsolutePath().toString()).collect(Collectors.toList()));

        return Stream.of(searchRoots)
                .map(JdkFinder::findLocalJdks)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static List<Result<LocalJavaRuntime>> findLocalJdks(final Path searchRoot) {
        if (Files.isDirectory(searchRoot)) {
            try {
                return Files.find(searchRoot, 5, JdkFinder::isJavaHome)
                        .map(Path::toAbsolutePath)
                        .map(Path::normalize)
                        .distinct()
                        .map(Result.of(JdkFinder::getLocalJavaRuntime))
                        .collect(Collectors.toList());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    private static boolean isJavaHome(final Path path, final BasicFileAttributes basicFileAttributes) {
        if (!basicFileAttributes.isDirectory()) {
            return false;
        }

        if (Files.isRegularFile(path.resolve("bin" + File.separatorChar + "java"))) {
            return true;
        }

        if (Files.isRegularFile(path.resolve("bin" + File.separatorChar + "java.exe"))) {
            return true;
        }

        return false;
    }

    private static LocalJavaRuntime getLocalJavaRuntime(final Path javaHome) {
        final JavaRuntimePropertiesDetector.JavaRuntimeProperties jreProps = JavaRuntimePropertiesDetector.getProperties(javaHome);
        final String version = jreProps.getVersion();
        final String vendor = jreProps.getVendor();
        return LocalJavaRuntime.createPreInstalled(version, LOCAL_OS, vendor, javaHome);
    }
}
