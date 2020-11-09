package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class MacRuntimeFinder extends BaseRuntimeFinder {

    private static final String MAC_JVM_BASEFOLDER = "/Library/Java/JavaVirtualMachines";
    private static final String MAC_HOMEBREW_JVM_BASEFOLDER = "/usr/local/Cellar/openjdk/";

    @Override
    Collection<Path> getDefaultLocationsDefault() {
        final Path systemPath = Paths.get(MAC_JVM_BASEFOLDER);
        final Path sdkmanPath = Paths.get(JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman");
        final Path homebrewPath = Paths.get(MAC_HOMEBREW_JVM_BASEFOLDER);

        return Arrays.asList(systemPath, sdkmanPath, homebrewPath);
    }

    @Override
    List<OperationSystem> getSupportedOperationSystems() {
        return Collections.singletonList(OperationSystem.MAC64);
    }
}
