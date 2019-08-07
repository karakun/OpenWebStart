package com.openwebstart.jvm.listener;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

public interface RuntimeAddedListener {

    void onRuntimeAdded(LocalJavaRuntime runtime);
}
