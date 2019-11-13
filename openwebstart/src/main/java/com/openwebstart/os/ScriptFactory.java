package com.openwebstart.os;

import com.openwebstart.install4j.Install4JUtils;
import net.sourceforge.jnlp.JNLPFile;

import java.util.Optional;

public class ScriptFactory {

    private static final String JAVA_WS_NAME = "javaws";

    public static String createStartCommand(final JNLPFile jnlpFile) {
        final String executable = Install4JUtils.installationDirectory()
                .map(d -> d + "/" + JAVA_WS_NAME)
                .orElseThrow(() -> new IllegalStateException("Can not define executable"));
        return executable + " \"" + Optional.ofNullable(jnlpFile.getSourceLocation()).orElse(jnlpFile.getFileLocation()) + "\"";
    }
}
