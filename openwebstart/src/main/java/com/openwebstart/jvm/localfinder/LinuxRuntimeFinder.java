package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class LinuxRuntimeFinder extends BaseRuntimeFinder {

    private static final String SYSTEM_VM_FOLDER = "/usr/lib/jvm/";
    private static final String SDK_MAN_FOLDER = JavaSystemProperties.getUserHome() + File.separatorChar + ".sdkman";

    @Override
    Collection<String> getDefaultLocations() {
        return Arrays.asList(SYSTEM_VM_FOLDER, SDK_MAN_FOLDER);
    }

    @Override
    List<OperationSystem> getSupportedOperationSystems() {
        return Arrays.asList(OperationSystem.LINUX32, OperationSystem.LINUX64);
    }
}
