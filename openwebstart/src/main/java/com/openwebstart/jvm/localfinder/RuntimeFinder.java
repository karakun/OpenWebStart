package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.Result;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.util.List;

public interface RuntimeFinder {

    List<Result<LocalJavaRuntime>> findLocalRuntimes() throws Exception;

    List<OperationSystem> getSupportedOperationSystems();
}
