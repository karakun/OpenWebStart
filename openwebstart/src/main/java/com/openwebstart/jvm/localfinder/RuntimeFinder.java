package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.nio.file.Path;
import java.util.List;

public interface RuntimeFinder {

    List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes() throws Exception;

    List<OperationSystem> getSupportedOperationSystems();
}
