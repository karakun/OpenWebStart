package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.Assert;

import java.io.Serializable;

public class JavaRuntime implements Serializable {

    private final String version;

    private final String vendor;

    private final OperationSystem operationSystem;

    public JavaRuntime(final String version, final OperationSystem operationSystem, final String vendor) {
        this.version = Assert.requireNonBlank(version, "version");
        this.operationSystem = Assert.requireNonNull(operationSystem, "operationSystem");
        this.vendor = Assert.requireNonBlank(vendor, "vendor");
    }

    public String getVersion() {
        return version;
    }

    public OperationSystem getOperationSystem() {
        return operationSystem;
    }

    public String getVendor() {
        return vendor;
    }

}

