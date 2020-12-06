package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class MacRuntimeFinder extends BaseRuntimeFinder {

    private static final String SYSTEM_VM_FOLDER = "/Library/Java/JavaVirtualMachines";
    private static final String HOMEBREW_JVM_FOLDER = "/usr/local/Cellar/openjdk/";
    private static final String SDK_MAN_FOLDER = JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman";

    @Override
    Collection<String> getDefaultLocations() {
        return Arrays.asList(SYSTEM_VM_FOLDER, HOMEBREW_JVM_FOLDER, SDK_MAN_FOLDER);
    }

    @Override
    List<OperationSystem> getSupportedOperationSystems() {
        return Collections.singletonList(OperationSystem.MAC64);
    }
}
