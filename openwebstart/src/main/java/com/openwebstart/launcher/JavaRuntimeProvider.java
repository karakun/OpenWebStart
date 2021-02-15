package com.openwebstart.launcher;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;
import java.util.Optional;

public interface JavaRuntimeProvider {
    Optional<LocalJavaRuntime> getJavaRuntime(VersionString version, Vendor vendor, URL url, boolean require32bit);
}
