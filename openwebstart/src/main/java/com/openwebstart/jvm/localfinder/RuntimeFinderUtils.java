package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.func.Result;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class RuntimeFinderUtils {

    public static List<Result<LocalJavaRuntime>> findRuntimesOnSystem() {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();
        final List<Result<LocalJavaRuntime>> foundRuntimes = new ArrayList<>();
        ServiceLoader.load(RuntimeFinder.class).iterator().forEachRemaining(f -> {
            if (f.getSupportedOperationSystems().contains(currentOs)) {
                try {
                    foundRuntimes.addAll(f.findLocalRuntimes());
                } catch (final Exception e) {
                    throw new RuntimeException("Error while searching for JVMs on the system", e);
                }
            }
        });
        return Collections.unmodifiableList(foundRuntimes);
    }
}
