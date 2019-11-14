package com.openwebstart.util;

import com.openwebstart.os.ScriptFactory;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class ProcessUtil {

    private final static Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    public static void logIO(final InputStream src) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final Scanner sc = new Scanner(src);
            while (sc.hasNextLine()) {
                LOG.debug("APP: " + sc.nextLine());
            }
        });
    }
}
