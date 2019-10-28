package com.openwebstart.install4j;

import com.install4j.api.launcher.Variables;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class Install4JUtils {

    private static final Logger LOG = LoggerFactory.getLogger(Install4JUtils.class);

    private static final String VERSION_VARIABLE_NAME = "sys.applicationVersion";

    public static Optional<String> applicationVersion() {
        try {
            return Optional.ofNullable(Variables.getCompilerVariable(VERSION_VARIABLE_NAME));
        } catch (IOException e) {
            LOG.warn("Can not read application applicationVersion");
            return Optional.empty();
        }
    }
}
