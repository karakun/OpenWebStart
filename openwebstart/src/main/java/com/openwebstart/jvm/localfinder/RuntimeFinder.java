package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuntimeFinder {

    private static final BaseRuntimeFinder[] FINDERS = {
            new WindowsRuntimeFinder(),
            new MacRuntimeFinder(),
            new LinuxRuntimeFinder()
    };

    public List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes(final DeploymentConfiguration deploymentConfiguration) {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();

        final List<ResultWithInput<Path, LocalJavaRuntime>> foundRuntimes = Stream.of(FINDERS)
                .filter(finder -> finder.getSupportedOperationSystems().contains(currentOs))
                .map(finder -> finder.findLocalRuntimes(deploymentConfiguration))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return Collections.unmodifiableList(foundRuntimes);
    }
}
