package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.util.JvmVersionUtils;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.io.Serializable;

public abstract class JavaRuntime implements Serializable {

    private final VersionId version;

    private final Vendor vendor;

    private final OperationSystem operationSystem;

    public static final String HTTP_AGENT_PROPERTY = "http.agent";

    public JavaRuntime(final JavaRuntime other) {
        this(other.version, other.operationSystem, other.vendor);
    }

    public JavaRuntime(final String version, final OperationSystem operationSystem, final String vendor) {
        this(JvmVersionUtils.fromString(version), operationSystem, Vendor.fromString(vendor));
    }

    private JavaRuntime(final VersionId version, final OperationSystem operationSystem, final Vendor vendor) {
        this.version = Assert.requireNonNull(version, "version");
        this.operationSystem = Assert.requireNonNull(operationSystem, "operationSystem");
        this.vendor = Assert.requireNonNull(vendor, "vendor");
    }

    public VersionId getVersion() {
        return version;
    }

    public OperationSystem getOperationSystem() {
        return operationSystem;
    }

    public Vendor getVendor() {
        return vendor;
    }

    @Override
    public String toString() {
        return "JavaRuntime{" +
                "version=" + version +
                ", vendor=" + vendor +
                ", operationSystem=" + operationSystem +
                '}';
    }
}

