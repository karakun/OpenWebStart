package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.Assert;

import java.net.MalformedURLException;
import java.net.URL;

public class RemoteJavaRuntime extends JavaRuntime {

    private final String href;

    public RemoteJavaRuntime(final String version, final OperationSystem operationSystem, final String vendor, final String href) {
        super(version, operationSystem, vendor);
        this.href = Assert.requireNonNull(href, "endpoint");
    }

    public String getHref() {
        return href;
    }

    public URL getEndpoint() throws MalformedURLException {
        return new URL(href);
    }
}
