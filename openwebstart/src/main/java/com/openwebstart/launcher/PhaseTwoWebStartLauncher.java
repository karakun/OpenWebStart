package com.openwebstart.launcher;

import com.openwebstart.install4j.Install4JUpdateHandler;
import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.update.UpdatePanelConfigConstants;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.naming.ConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.runtime.ForkingStrategy.ALWAYS;


/**
 * This launcher resolves OS specific command line argument handling and starts Iced-Tea Web with the correct
 * argument arrangement.
 */
public class PhaseTwoWebStartLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(PhaseTwoWebStartLauncher.class);
    private static final String consoleOption = "-console";
    private static final List<String> optionsToSkip = Arrays.asList(CommandLineOptions.NOFORK.getOption(), CommandLineOptions.VIEWER.getOption(), consoleOption);

    public static void main(final String[] args) {
        Install4JUtils.applicationVersion().ifPresent(v -> LOG.info("Starting OpenWebStart {}", v));

        Translator.addBundle("i18n");

        final DeploymentConfiguration config = new DeploymentConfiguration();
        try {
            config.load();
        } catch (final ConfigurationException e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("error.loadConfig"), e);
            JNLPRuntime.exit(-1);
        }

        try {
            new InitialConfigurationCheck(config).check();
        } catch (final Exception e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("error.initialConfig"), e);
            JNLPRuntime.exit(-1);
        } catch (final UnsatisfiedLinkError e) {
            //TODO: this exception is thrown on windows if you start OWS from the ide instead of using install4J
            LOG.error("Initial configuration was not checked. This normally happens on Windows systems if you start OWS from the IDE.", e);
        }

        if (UpdatePanelConfigConstants.isAutoUpdateActivated(config)) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    new Install4JUpdateHandler(UpdatePanelConfigConstants.getUpdateScheduleForLauncher(config)).triggerPossibleUpdate();
                } catch (Exception e) {
                    LOG.error("Error in possible update process", e);
                }
            });
        }

        // we MUST fork in order to start the application with the jvm from the JVM Manager
        JNLPRuntime.setForkingStrategy(ALWAYS);

        final List<String> bootArgs = skipNotRelevantArgs(args);
        LOG.info("Calling ITW Boot with args {}.", bootArgs);
        Boot.main(bootArgs.toArray(new String[0]));

        System.exit(0);
    }

    private static List<String> skipNotRelevantArgs(final String[] args) {
        final List<String> relevantJavawsArgs = Arrays.stream(args)
                .filter(arg -> !optionsToSkip.contains(arg))
                .collect(Collectors.toList());

        LOG.debug("RelevantJavawsArgs: '{}'", relevantJavawsArgs);

        return relevantJavawsArgs;
    }
}
