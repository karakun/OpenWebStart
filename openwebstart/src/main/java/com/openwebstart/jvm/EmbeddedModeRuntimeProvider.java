package com.openwebstart.jvm;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.jvm.util.JavaRuntimePropertiesDetector;
import com.openwebstart.jvm.util.JavaRuntimePropertiesDetector.JavaRuntimeProperties;
import com.openwebstart.launcher.JavaRuntimeProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

class EmbeddedModeRuntimeProvider implements JavaRuntimeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedModeRuntimeProvider.class);

    private static final OperationSystem LOCAL_OS = OperationSystem.getLocalSystem();

    private final LocalJavaRuntime localJavaRuntime;

    EmbeddedModeRuntimeProvider() {
        final Path currentJavaHome = Paths.get(JavaSystemProperties.getJavaHome());
        final JavaRuntimeProperties javaRuntimeProperties = JavaRuntimePropertiesDetector.getProperties(currentJavaHome);
        final OperationSystem currentOs = OperationSystem.getOperationSystem(javaRuntimeProperties.getOsName(), javaRuntimeProperties.getOsArch()).orElse(LOCAL_OS);

        localJavaRuntime = new LocalJavaRuntime(javaRuntimeProperties.getVersion(), currentOs, javaRuntimeProperties.getVendor(), currentJavaHome, LocalDateTime.now(), true, false);
    }

    @Override
    public void touch(LocalJavaRuntime javaRuntime) {
        // do nothing
    }

    @Override
    public Optional<LocalJavaRuntime> getJavaRuntime(final VersionString versionString, final Vendor vendorFromJnlp, final URL serverEndpointFromJnlp) {
        Assert.requireNonNull(versionString, "versionString");

        LOG.debug("Trying to match Java runtime. Requested version: '{}'", versionString);

        if (versionString.contains(localJavaRuntime.getVersion())) {
            return Optional.of(localJavaRuntime);
        }

        LOG.info("Local runtime version ({}) does not match requested version {}", localJavaRuntime.getVersion(), versionString);
        return Optional.empty();
    }
}
