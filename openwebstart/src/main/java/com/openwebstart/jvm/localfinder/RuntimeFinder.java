package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public interface RuntimeFinder {

    List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes() throws Exception;

    List<OperationSystem> getSupportedOperationSystems();

    static List<ResultWithInput<Path, LocalJavaRuntime>> find() {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();

        final List<ResultWithInput<Path,LocalJavaRuntime>> foundRuntimes = new ArrayList<>();
        ServiceLoader.load(RuntimeFinder.class).iterator().forEachRemaining(f -> {
            if (f.getSupportedOperationSystems().contains(currentOs)) {
                try {
                    foundRuntimes.addAll(f.findLocalRuntimes());
                } catch (final Exception e) {
                    throw new RuntimeException("Error while searching for local JVMs", e);
                }
            }
        });
        return Collections.unmodifiableList(foundRuntimes);
    }
}
