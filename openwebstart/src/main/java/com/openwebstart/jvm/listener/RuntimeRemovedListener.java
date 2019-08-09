package com.openwebstart.jvm.listener;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

public interface RuntimeRemovedListener {

    void onRuntimeRemoved(LocalJavaRuntime runtime);

}
