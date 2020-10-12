package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface RuntimeFinder {

    static List<ResultWithInput<Path, LocalJavaRuntime>> find() {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();

        final List<ResultWithInput<Path, LocalJavaRuntime>> foundRuntimes = new ArrayList<>();

        if (WindowsRuntimeFinder.getSupportedOperationSystems().contains(currentOs)) {
            foundRuntimes.addAll(WindowsRuntimeFinder.findLocalRuntimes());
        } else if (LinuxRuntimeFinder.getSupportedOperationSystems().contains(currentOs)) {
            foundRuntimes.addAll(LinuxRuntimeFinder.findLocalRuntimes());
        } else if (MacRuntimeFinder.getSupportedOperationSystems().contains(currentOs)) {
            foundRuntimes.addAll(MacRuntimeFinder.findLocalRuntimes());
        }
        return Collections.unmodifiableList(foundRuntimes);
    }
}
