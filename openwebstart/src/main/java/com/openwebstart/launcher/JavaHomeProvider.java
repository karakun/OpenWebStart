package com.openwebstart.launcher;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;
import java.nio.file.Path;

public interface JavaHomeProvider {
    Path getJavaHome(VersionString version, URL url);
}
