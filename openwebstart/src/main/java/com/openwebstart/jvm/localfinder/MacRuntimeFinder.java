package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.func.Result;
import com.openwebstart.jvm.localfinder.JavaRuntimePropertiesDetector.JavaRuntimeProperties;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.util.WebstartUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MacRuntimeFinder implements RuntimeFinder {

    private static final Logger LOG = LoggerFactory.getLogger(MacRuntimeFinder.class);

    private static final String MAC_JVM_BASEFOLDER = "/Library/Java/JavaVirtualMachines";

    private static final String MAC_JVM_CONTENT_FOLDER = "Contents/Home";

    @Override
    public List<Result<LocalJavaRuntime>> findLocalRuntimes() throws Exception {
        LOG.debug("Searching for local runtimes");
        final Path basePath = Paths.get(MAC_JVM_BASEFOLDER);
        if (Files.isDirectory(basePath)) {
            return Files.list(basePath)
                    .filter(Files::isDirectory)
                    .map(p -> Paths.get(p.toString(), MAC_JVM_CONTENT_FOLDER))
                    .filter(Files::isDirectory)
                    .map(Result.of(p -> {
                        final JavaRuntimeProperties jreProps = JavaRuntimePropertiesDetector.getProperties(p);
                        final String version = jreProps.getVersion();
                        final String vendor = jreProps.getVendor();
                        final String formattedVersion = WebstartUtils.convertJavaVersion(version);
                        return LocalJavaRuntime.createPreInstalled(formattedVersion, OperationSystem.MAC64, vendor, p);
                    })).collect(Collectors.toList());
        } else {
            LOG.debug("No runtime found");
            return Collections.emptyList();
        }
    }

    @Override
    public List<OperationSystem> getSupportedOperationSystems() {
        return Collections.singletonList(OperationSystem.MAC64);
    }
}
