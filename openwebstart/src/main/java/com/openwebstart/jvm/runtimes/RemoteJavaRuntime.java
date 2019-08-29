package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.Assert;

import java.net.URL;

public class RemoteJavaRuntime extends JavaRuntime {

    private final URL endpoint;

    public RemoteJavaRuntime(final String version, final OperationSystem operationSystem, final String vendor, final URL endpoint) {
        super(version, operationSystem, vendor);
        this.endpoint = Assert.requireNonNull(endpoint, "endpoint");
    }

    public URL getEndpoint() {
        return endpoint;
    }
}
