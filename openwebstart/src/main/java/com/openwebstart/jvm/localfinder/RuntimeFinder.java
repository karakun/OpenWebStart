package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.func.Result;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.os.OperationSystem;

import java.util.List;

public interface RuntimeFinder {

    List<Result<LocalJavaRuntime>> findLocalRuntimes() throws Exception;

    List<OperationSystem> getSupportedOperationSystems();
}
