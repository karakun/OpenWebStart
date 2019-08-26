package com.openwebstart.launcher;

import com.install4j.api.launcher.StartupNotification;
import com.install4j.runtime.installer.helper.InstallerUtil;
import com.openwebstart.jvm.JavaRuntimeSelector;
import com.openwebstart.jvm.LocalRuntimeManager;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.Boot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This launcher resolves OS specific command line argument handling and starts Iced-Tea Web with the correct
 * argument arrangement.
 */
public class OpenWebStartLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebStartLauncher.class);

    public static void main(String[] args) throws Exception {

        LOG.info("OpenWebStartLauncher called with args {}.", Arrays.toString(args));
        LOG.debug("OS detected: Win[{}], MacOS[{}], Linux[{}]",
                InstallerUtil.isWindows(), InstallerUtil.isMacOS(), InstallerUtil.isLinux());

        final List<String> bootArgs = skipNotRelevantArgs(args);
        final JavaHomeProvider javaHomeProvider = JavaRuntimeSelector.getInstance();
        LocalRuntimeManager.getInstance().loadRuntimes();

        // TODO:
        //  JavaRuntimeSelector.setDownloadHandler(JvmManagerDemo::showDownloadDialog);
        //  JavaRuntimeSelector.setAskForUpdateFunction(JvmManagerDemo::askForUpdate);

        /**
         * Listener will be called when the executable is started again or when a file open event is received.
         * Note that each invocation may be from a separate thread, therefore the implementation needs to be
         * synchronized.
         */
        StartupNotification.registerStartupListener(new StartupNotification.Listener() {
            public void startupPerformed(String parameters) {
                synchronized (this) {

                    if (InstallerUtil.isMacOS()) {
                        LOG.info("MacOS detected, Launcher needs to add JNLP file name {} to the list of arguments.", parameters);
                        Collections.addAll(bootArgs, parameters); // add file name at the end file to open

                        LOG.info("ITW Boot called with custom OwsJvmLauncher and args {}.", bootArgs);
                        Boot.main(new OwsJvmLauncher(javaHomeProvider), bootArgs.toArray(new String[0]));
                    }
                }
            }
        });

        // Windows and Linux are called here
        if (!InstallerUtil.isMacOS()) {
            LOG.info("ITW Boot called with custom OwsJvmLauncher and args {}.", Arrays.toString(args));
            Boot.main(new OwsJvmLauncher(javaHomeProvider), bootArgs.toArray(new String[0]));
        }
    }


    private static List<String> skipNotRelevantArgs(final String[] args) {
        final List<String> relevantJavawsArgs = Arrays.stream(args)
                .filter(arg -> !arg.equals(CommandLineOptions.NOFORK.getOption()))
                .collect(Collectors.toList());

        LOG.debug("RelevantJavawsArgs: '{}'", relevantJavawsArgs);

        return relevantJavawsArgs;
    }
}
