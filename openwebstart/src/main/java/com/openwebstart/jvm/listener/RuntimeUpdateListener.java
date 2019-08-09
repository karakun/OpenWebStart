package com.openwebstart.jvm.listener;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

public interface RuntimeUpdateListener {

    void onRuntimeUpdated(LocalJavaRuntime oldValue, LocalJavaRuntime newValue);

}
