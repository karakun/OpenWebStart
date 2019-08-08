package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.os.OperationSystem;
import dev.rico.core.functional.Result;
import dev.rico.internal.core.http.ConnectionUtils;
import net.adoptopenjdk.icedteaweb.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

//TODO: "java -XshowSettings:properties -version"

public interface RuntimeFinder {

    String RELEASE_FILE_VERSION_PROPERTY = "JAVA_VERSION=";

    String RELEASE_FILE_VENDOR_PROPERTY = "IMPLEMENTOR=";
    
    List<Result<LocalJavaRuntime>> findLocalRuntimes() throws Exception;

    List<OperationSystem> getSupportedOperationSystems();

    static String readVendor(final Path javaHomeDir) throws Exception{
        final String content = getReleaseFileContent(javaHomeDir);
        return Arrays.stream(content.split(System.lineSeparator()))
                .filter(l -> l.startsWith(RELEASE_FILE_VENDOR_PROPERTY))
                .map(l -> l.substring(RELEASE_FILE_VENDOR_PROPERTY.length() + 1, l.length() - 1))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Can not extract vendor from release file for JAVAHOME=" + javaHomeDir));
    }

    static String readVersion(final Path javaHomeDir) throws Exception{
        final String content = getReleaseFileContent(javaHomeDir);
        return Arrays.stream(content.split(System.lineSeparator()))
                .filter(l -> l.startsWith(RELEASE_FILE_VERSION_PROPERTY))
                .map(l -> l.substring(RELEASE_FILE_VERSION_PROPERTY.length() + 1, l.length() - 1))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Can not read version from release file for JAVAHOME=" + javaHomeDir));
    }

    static String getReleaseFileContent(final Path javaHomeDir) throws Exception{
        Assert.requireNonNull(javaHomeDir, "javaHomeDir");
        final Path releaseDocPath = Paths.get(javaHomeDir.toString(), "release");
        return ConnectionUtils.readUTF8Content(Files.newInputStream(releaseDocPath));
    }

    static List<Result<LocalJavaRuntime>> findRuntimesOnSystem() {
        final OperationSystem currentOs = OperationSystem.getLocalSystem();
        final List<Result<LocalJavaRuntime>> foundRuntimes = new ArrayList<>();
        ServiceLoader.load(RuntimeFinder.class).iterator().forEachRemaining(f ->{
            if(f.getSupportedOperationSystems().contains(currentOs)) {
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
