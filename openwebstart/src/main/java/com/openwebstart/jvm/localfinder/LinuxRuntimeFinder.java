package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class LinuxRuntimeFinder extends BaseRuntimeFinder {

    private static final String JVM_BASE_FOLDER = "/usr/lib/jvm/";

    @Override
    Collection<Path> getDefaultLocationsDefault() {
        final Path systemPath = Paths.get(JVM_BASE_FOLDER);
        final Path sdkmanPath = Paths.get(JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman");
        return Arrays.asList(systemPath, sdkmanPath);
    }

    @Override
    List<OperationSystem> getSupportedOperationSystems() {
        return Arrays.asList(OperationSystem.LINUX32, OperationSystem.LINUX64);
    }
}
