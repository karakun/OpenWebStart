package com.openwebstart.os;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.jvm.os.OperationSystem;

import net.sourceforge.jnlp.JNLPFile;

public class ScriptFactory 
{
    private final static String SCRIPT_START = "#!/bin/sh";

    private static final String JAVA_WS_NAME = "javaws";

    public static String createStartScript(final JNLPFile jnlpFile) {
        if(OperationSystem.getLocalSystem() == OperationSystem.MAC64) {
            return createSimpleStartScriptForMac(jnlpFile);
        }
        final String executable = Install4JUtils.installationDirectory()
                .map(d -> d + "/" + JAVA_WS_NAME)
                .orElseThrow(() -> new IllegalStateException("Can not define executable"));
        final String jnlpLocation = "\"" + Optional.ofNullable(jnlpFile.getSourceLocation()).orElse(jnlpFile.getFileLocation()) + "\"";
        return executable + " " + jnlpLocation;
    }

    public static String createStartScriptForMac(final JNLPFile jnlpFile) {
        final String executable = "\"" + Install4JUtils.installationDirectory()
                .map(d -> d + "/" + "OpenWebStart javaws.app" + "\"")
                .orElseThrow(() -> new IllegalStateException("Can not define executable"));

        //TODO: URL is not working on mac. Maybe we can add a new param to OWS that can be used to pass
        // the jnlp url (mac open command supports --args)
        // This one is not working: open -a "/Applications/OpenWebStart/OpenWebStart javaws.app" --args -jnlp "file:/Users/hendrikebbers/Desktop/AccessibleScrollDemo.jnlpx"
        final String jnlpLocation = "\"" + jnlpFile.getFileLocation() + "\"";

        return SCRIPT_START + System.lineSeparator() + "open -a " + executable + " " + jnlpLocation;
    }

	public static String createSimpleStartScriptForMac(final JNLPFile jnlpFile) {
		final URL srcUrl = jnlpFile.getSourceLocation();
		final String schema = srcUrl.getProtocol();
		final String url;
		if ("http".equalsIgnoreCase(schema) || "https".equalsIgnoreCase(schema)) {
			url = "jnlp" + srcUrl.toString().substring(4);
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
