package com.openwebstart.install4j;

import com.install4j.api.launcher.Variables;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class Install4JUtils {

    private static final Logger LOG = LoggerFactory.getLogger(Install4JUtils.class);

    private static final String VERSION_VARIABLE_NAME = "sys.applicationVersion";

    private static final String UPDATES_URL_VARIABLE_NAME = "sys.updatesUrl";

    public static Optional<String> applicationVersion() {
        try {
            return Optional.ofNullable(Variables.getCompilerVariable(VERSION_VARIABLE_NAME));
        } catch (IOException e) {
            LOG.warn("Can not read application applicationVersion");
            return Optional.empty();
        }
    }

    public static String updatesUrl() throws IllegalStateException{
        try {
            final String value = Variables.getCompilerVariable(UPDATES_URL_VARIABLE_NAME);
            if(value == null) {
                throw new IllegalStateException("No update url defined");
            }
            return value;
        } catch (Exception e) {
            throw new IllegalStateException("Can not get update url", e);
        }
    }
}
