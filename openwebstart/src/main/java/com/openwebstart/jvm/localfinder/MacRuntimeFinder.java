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
import java.util.Collections;
import java.util.List;

public class MacRuntimeFinder implements RuntimeFinder {
    private static final Logger LOG = LoggerFactory.getLogger(MacRuntimeFinder.class);

    private static final String MAC_JVM_BASEFOLDER = "/Library/Java/JavaVirtualMachines";
    private static final String MAC_HOMEBREW_JVM_BASEFOLDER = "/usr/local/Cellar/openjdk/";


    @Override
    public List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes() {
        LOG.debug("Searching for local runtimes");

        final Path systemPath = Paths.get(MAC_JVM_BASEFOLDER);
        final Path sdkmanPath = Paths.get(JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman");
        final Path homebrewPath = Paths.get(MAC_HOMEBREW_JVM_BASEFOLDER);

        return JdkFinder.findLocalJdks(systemPath, sdkmanPath, homebrewPath);
    }

    @Override
    public List<OperationSystem> getSupportedOperationSystems() {
        return Collections.singletonList(OperationSystem.MAC64);
    }
}
