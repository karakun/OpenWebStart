package com.openwebstart.launcher;

import com.install4j.runtime.installer.helper.InstallerUtil;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.dialogs.RuntimeDownloadDialog;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.runtime.ForkingStrategy.ALWAYS;


/**
 * This launcher resolves OS specific command line argument handling and starts Iced-Tea Web with the correct
 * argument arrangement.
 */
public class PhaseTwoWebStartLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(PhaseTwoWebStartLauncher.class);

    public static void main(String[] args) {
        Translator.addBundle("i18n");

        LOG.info("OpenWebStartLauncher called with args {}.", Arrays.toString(args));
        LOG.debug("OS detected: Win[{}], MacOS[{}], Linux[{}]",
                InstallerUtil.isWindows(), InstallerUtil.isMacOS(), InstallerUtil.isLinux());

        final List<String> bootArgs = skipNotRelevantArgs(args);
        final JavaRuntimeProvider javaRuntimeProvider = JavaRuntimeManager.getJavaRuntimeProvider(
                RuntimeDownloadDialog::showDownloadDialog,
                DialogFactory::askForRuntimeUpdate
        );

        JNLPRuntime.setForkingStrategy(ALWAYS);

        LOG.info("ITW Boot called with custom OwsJvmLauncher and args {}.", Arrays.toString(args));
        Boot.main(new OwsJvmLauncher(javaRuntimeProvider), bootArgs.toArray(new String[0]));
    }


    private static List<String> skipNotRelevantArgs(final String[] args) {
        final List<String> relevantJavawsArgs = Arrays.stream(args)
                .filter(arg -> !arg.equals(CommandLineOptions.NOFORK.getOption()))
                .collect(Collectors.toList());

        LOG.debug("RelevantJavawsArgs: '{}'", relevantJavawsArgs);

        return relevantJavawsArgs;
    }
}
