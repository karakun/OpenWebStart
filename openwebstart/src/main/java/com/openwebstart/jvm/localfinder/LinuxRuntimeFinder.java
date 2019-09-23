package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

public class LinuxRuntimeFinder implements RuntimeFinder {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxRuntimeFinder.class);

    private static final String JVM_BASEFOLDER = "/usr/lib/jvm/";

    @Override
    public List<Result<LocalJavaRuntime>> findLocalRuntimes() {
        LOG.debug("Searching for local runtimes");

        final Path systemPath = Paths.get(JVM_BASEFOLDER);
        final Path sdkmanPath = Paths.get(JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman");

        return JdkFinder.findLocalJdks(systemPath, sdkmanPath);
    }

    @Override
    public List<OperationSystem> getSupportedOperationSystems() {
        return Arrays.asList(OperationSystem.LINUX32, OperationSystem.LINUX64);
    }
}
