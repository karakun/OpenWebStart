package com.openwebstart.jvm.json;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CacheStore implements Serializable {

    private final List<LocalJavaRuntime> runtimes;

    public CacheStore(final List<LocalJavaRuntime> runtimes) {
        this.runtimes = Collections.unmodifiableList(runtimes);
    }

    public List<LocalJavaRuntime> getRuntimes() {
        return runtimes;
    }
}
