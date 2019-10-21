package com.openwebstart.launcher;

import com.install4j.api.launcher.StartupNotification;
import com.install4j.runtime.installer.helper.InstallerUtil;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;


/**
 * This launcher resolves OS specific command line argument handling and starts Iced-Tea Web with the correct
 * argument arrangement.
 */
public class OpenWebStartLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebStartLauncher.class);

    public static void main(String[] args) {
        if (!InstallerUtil.isMacOS()) {
            LOG.info("ITW Boot called with custom OwsJvmLauncher and args {}.", Arrays.toString(args));
            PhaseTwoWebStartLauncher.main(args);
        } else {
            StartupNotification.registerStartupListener(parameters -> {
                try {
                    final List<String> mergedArgs = new ArrayList<>(Arrays.asList(args));
                    LOG.info("MacOS detected, Launcher needs to add JNLP file name {} to the list of arguments.", parameters);
                    Collections.addAll(mergedArgs, parameters); // add file name at the end file to open
                    LOG.info("ITW Boot called with custom OwsJvmLauncher and args {}.", mergedArgs);

                    final List<String> commands = new ArrayList<>();
                    commands.add(quoteIfRequired(JavaSystemProperties.getJavaHome() + "/bin/java"));
                    commands.add("-cp");
                    commands.add(JavaSystemProperties.getJavaClassPath());
                    commands.add(PhaseTwoWebStartLauncher.class.getName());
                    commands.addAll(mergedArgs);

                    LOG.info("Starting: " + commands);
                    new ProcessBuilder().command(commands).inheritIO().start();
                } catch (Exception e) {
                    LOG.error("Error in starting JNLP application", e);
                    throw new RuntimeException("Error in starting JNLP application", e);
                }
            });
        }
    }
}
