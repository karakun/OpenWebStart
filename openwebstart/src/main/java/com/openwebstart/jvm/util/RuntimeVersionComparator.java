package com.openwebstart.jvm.util;

import com.openwebstart.jvm.runtimes.JavaRuntime;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionIdComparator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.util.Comparator;

public class RuntimeVersionComparator implements Comparator<JavaRuntime> {

    private final VersionIdComparator versionIdComparator;

    public RuntimeVersionComparator(final VersionString versionString) {
        this.versionIdComparator = new VersionIdComparator(versionString);
    }

    @Override
    public int compare(final JavaRuntime o1, final JavaRuntime o2) {
        return versionIdComparator.compare(o1.getVersion(), o2.getVersion());
    }
}
