package com.openwebstart.jvm.util;

import com.openwebstart.jvm.runtimes.JavaRuntime;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
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
        final VersionId version1 = VersionId.fromString(o1.getVersion());
        final VersionId version2 = VersionId.fromString(o2.getVersion());

        return versionIdComparator.compare(version1, version2);
    }
}
