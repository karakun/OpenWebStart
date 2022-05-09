package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.util.JavaRuntimePropertiesDetector;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.openwebstart.jvm.os.OperationSystem.getOperationSystem;

public class JdkFinder {

    private static final Logger LOG = LoggerFactory.getLogger(JdkFinder.class);

    private static final OperationSystem LOCAL_OS = OperationSystem.getLocalSystem();

    private static final BaseRuntimeFinder[] FINDERS = {
            new WindowsRuntimeFinder(),
            new MacRuntimeFinder(),
            new LinuxRuntimeFinder()
    };

    public static List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes(final DeploymentConfiguration deploymentConfiguration) {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();

        final List<ResultWithInput<Path, LocalJavaRuntime>> foundRuntimes = Stream.of(FINDERS)
                .filter(finder -> finder.getSupportedOperationSystems().contains(currentOs))
                .map(finder -> finder.findLocalRuntimes(deploymentConfiguration))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return Collections.unmodifiableList(foundRuntimes);
    }

    public static List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes(final Path searchRoot) {
        LOG.debug("About to look for local JVMs at the following location: {}", searchRoot);

        if (Files.isDirectory(searchRoot)) {
            try {
                return Files.find(searchRoot, 5, JdkFinder::isJavaHome)
                        .map(Path::toAbsolutePath)
                        .map(Path::normalize)
                        .distinct()
                        .map(Result.withInput(JdkFinder::getLocalJavaRuntime))
                        .collect(Collectors.toList());
            } catch (final IOException e) {
                throw new RuntimeException("Error while searching for local JVMs at '" + searchRoot + "'", e);
            }
        }
        return Collections.emptyList();
    }

    public static boolean isJavaHome(final Path path, final BasicFileAttributes basicFileAttributes) {
        if (!basicFileAttributes.isDirectory()) {
            return false;
        }

        if (Files.isRegularFile(path.resolve("bin" + File.separatorChar + "java"))) {
            return true;
        }

        return Files.isRegularFile(path.resolve("bin" + File.separatorChar + "java.exe"));
    }

    private static LocalJavaRuntime getLocalJavaRuntime(final Path javaHome) {
        Assert.requireNonNull(javaHome, "javaHome");
        if (isInternalJvm(javaHome)) {
            LOG.info("JVM '{}' won't be used since it is the internal OpenWebStart JVM", javaHome);
            throw new IllegalArgumentException("The selected JVM at '" + javaHome + "' is the internal OpenWebStart JVM");
        }
        try {
            final JavaRuntimePropertiesDetector.JavaRuntimeProperties jreProps = JavaRuntimePropertiesDetector.getProperties(javaHome);
            final String version = jreProps.getVersion();
            final String vendor = jreProps.getVendor();
            final OperationSystem os = getOperationSystem(jreProps.getOsName(), jreProps.getOsArch(), jreProps.getBitness()).orElse(LOCAL_OS);
            return LocalJavaRuntime.createPreInstalled(version, os, vendor, javaHome);
        } catch (final Exception e) {
            throw new IllegalStateException("Error while reading properties from JVM at '" + javaHome + "'", e);
        }
    }

    private static boolean isInternalJvm(final Path javaHome) {
        return Objects.equals(Paths.get(JavaSystemProperties.getJavaHome()), javaHome);
    }
}
