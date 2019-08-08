package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.util.WebstartUtils;
import dev.rico.core.functional.Result;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
                    .filter(p -> Files.isDirectory(p))
                    .filter(p -> {
                        final Path jrePath = Paths.get(p.toString(), MAC_JVM_CONTENT_FOLDER);
                        return Files.isDirectory(jrePath);
                    }).map(p -> Paths.get(p.toString(), MAC_JVM_CONTENT_FOLDER))
                    .filter(p -> {
                        final Path releaseDocPath = Paths.get(p.toString(), "release");
                        return Files.exists(releaseDocPath);
                    }).map(Result.of(p -> {
                        final String version = RuntimeFinder.readVersion(p);
                        final String vendor = RuntimeFinder.readVendor(p);
                        final String formatedVersion = WebstartUtils.convertJavaVersion(version);
                        return new LocalJavaRuntime(formatedVersion, OperationSystem.MAC64, vendor, p, LocalDateTime.now(), false, false);
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
