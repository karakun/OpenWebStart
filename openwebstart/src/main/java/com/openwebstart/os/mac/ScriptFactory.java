package com.openwebstart.os.mac;

import net.sourceforge.jnlp.JNLPFile;

import java.net.URL;

public class ScriptFactory {
    private static final String SCRIPT_START = "#!/bin/sh";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String JNLP = "jnlp";

    static String createSimpleStartScriptForMac(final JNLPFile jnlpFile) {
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

}
