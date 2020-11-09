package com.openwebstart.jvm.localfinder;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class BaseRuntimeFinder {
    private static final Logger LOG = LoggerFactory.getLogger(BaseRuntimeFinder.class);

    List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes(DeploymentConfiguration config) {
        LOG.debug("Searching for local runtimes");

        final List<Path> pathToSearchIn = new ArrayList<>();

        pathToSearchIn.addAll(defaultPaths(config));
        pathToSearchIn.addAll(customPaths(config));

        return JdkFinder.findLocalJdks(pathToSearchIn);
    }

    private Collection<Path> defaultPaths(DeploymentConfiguration config) {
        final String excludeDefaultLocation = config.getProperty(OwsDefaultsProvider.EXCLUDE_DEFAULT_JVM_LOCATION);
        if (Boolean.parseBoolean(excludeDefaultLocation)) {
            return Collections.emptyList();
        }

        return pathsFromStrings(getDefaultLocations());
    }

    private List<Path> customPaths(DeploymentConfiguration config) {
        final List<String> paths = config.getPropertyAsList(OwsDefaultsProvider.CUSTOM_JVM_LOCATION);
        return pathsFromStrings(paths);
    }

    private List<Path> pathsFromStrings(Collection<String> paths) {
        return paths.stream()
                .filter(s -> !StringUtils.isBlank(s))
                .map(this::pathFromString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Path> pathFromString(String path) {
        try {
            return Optional.of(Paths.get(path));
        } catch (Exception e) {
            LOG.warn("Invalid JVM location: {}", e.getMessage());
            return Optional.empty();
        }
    }

    abstract Collection<String> getDefaultLocations();

    abstract List<OperationSystem> getSupportedOperationSystems();
}
