package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.util.VersionUtil;
import net.adoptopenjdk.icedteaweb.Assert;

import java.io.Serializable;

public class JavaRuntime implements Comparable<JavaRuntime>, Serializable {

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

    @Override
    public int compareTo(final JavaRuntime o) {
        if (o != null) {
            return VersionUtil.versionCompare(o.getVersion(), getVersion());
        }
        return 1;
    }
}

