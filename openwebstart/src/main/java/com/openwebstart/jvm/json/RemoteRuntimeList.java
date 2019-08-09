package com.openwebstart.jvm.json;

import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class RemoteRuntimeList implements Serializable {

    private final List<RemoteJavaRuntime> runtimes;

    private final long cacheTimeInMillis;

    public RemoteRuntimeList(final List<RemoteJavaRuntime> runtimes, final long cacheTimeInMillis) {
        this.runtimes = Collections.unmodifiableList(runtimes);
        this.cacheTimeInMillis = cacheTimeInMillis;
    }

    public List<RemoteJavaRuntime> getRuntimes() {
        return runtimes;
    }

    public long getCacheTimeInMillis() {
        return cacheTimeInMillis;
    }
}
