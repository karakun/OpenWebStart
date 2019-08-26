package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.Assert;

import java.net.URL;

public class RemoteJavaRuntime extends JavaRuntime {

    private final String hash;

    private final URL endpoint;

    public RemoteJavaRuntime(final String version, final OperationSystem operationSystem, final String vendor, final String hash, final URL endpoint) {
        super(version, operationSystem, vendor);
        this.hash = Assert.requireNonBlank(hash, "hash");
        this.endpoint = Assert.requireNonNull(endpoint, "endpoint");
    }

    public String getHash() {
        return hash;
    }

    public URL getEndpoint() {
        return endpoint;
    }
}
