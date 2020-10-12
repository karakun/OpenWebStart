package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

class LinuxRuntimeFinder {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxRuntimeFinder.class);

    private static final String JVM_BASEFOLDER = "/usr/lib/jvm/";

    static List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes() {
        LOG.debug("Searching for local runtimes");

        final Path systemPath = Paths.get(JVM_BASEFOLDER);
        final Path sdkmanPath = Paths.get(JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman");

        return JdkFinder.findLocalJdks(systemPath, sdkmanPath);
    }

    static List<OperationSystem> getSupportedOperationSystems() {
        return Arrays.asList(OperationSystem.LINUX32, OperationSystem.LINUX64);
    }
}
