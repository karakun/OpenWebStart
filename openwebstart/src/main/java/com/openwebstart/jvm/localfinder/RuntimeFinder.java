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

public interface RuntimeFinder {

    List<Result<LocalJavaRuntime>> findLocalRuntimes() throws Exception;

    List<OperationSystem> getSupportedOperationSystems();

}
