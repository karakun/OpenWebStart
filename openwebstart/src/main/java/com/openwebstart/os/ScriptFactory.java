package com.openwebstart.os;

import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.jvm.os.OperationSystem;
import net.sourceforge.jnlp.JNLPFile;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class ScriptFactory {
    private static final String SCRIPT_START = "#!/bin/sh";

    private static final String JAVA_WS_NAME = "javaws";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String JNLP = "jnlp";

    public static String createStartScript(final JNLPFile jnlpFile) {
        if (OperationSystem.getLocalSystem() == OperationSystem.MAC64) {
            return createSimpleStartScriptForMac(jnlpFile);
        }
        final String executable = Install4JUtils.installationDirectory()
                .map(d -> d + "/" + JAVA_WS_NAME)
                .orElseThrow(() -> new IllegalStateException("Can not define executable"));
        final String jnlpLocation = "\"" + Optional.ofNullable(jnlpFile.getSourceLocation()).orElse(jnlpFile.getFileLocation()) + "\"";
        return executable + " " + jnlpLocation;
    }

    public static String createSimpleStartScriptForMac(final JNLPFile jnlpFile) {
        final URL srcUrl = jnlpFile.getSourceLocation();
        final String schema = srcUrl.getProtocol();
        final String url;
        if (HTTP.equalsIgnoreCase(schema) || HTTPS.equalsIgnoreCase(schema)) {
            url = JNLP + srcUrl.toString().substring(4);
        } else {
            url = srcUrl.toString();
        }
        return SCRIPT_START + System.lineSeparator() + "open \"" + url + "\"";
    }

    public static Process createStartProcess(final JNLPFile jnlpFile) throws IOException {
        final String executable = "\"" + Install4JUtils.installationDirectory()
                .map(d -> d + "/" + "OpenWebStart javaws.app" + "\"")
                .orElseThrow(() -> new IllegalStateException("Can not define executable"));
        final String fileLocation = "\"" + jnlpFile.getFileLocation() + "\"";
        final ProcessBuilder builder = new ProcessBuilder();
        builder.command("open", "-a", executable, "--args", fileLocation);
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        return process;
    }

}
