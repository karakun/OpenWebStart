package com.openwebstart.launcher;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;

public interface JavaRuntimeProvider {
    LocalJavaRuntime getJavaRuntime(VersionString version, URL url);
}
