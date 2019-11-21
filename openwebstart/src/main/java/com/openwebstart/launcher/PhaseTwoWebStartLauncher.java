package com.openwebstart.launcher;

import com.install4j.runtime.installer.helper.InstallerUtil;
import com.openwebstart.install4j.Install4JUpdateHandler;
import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.dialogs.RuntimeDownloadDialog;
import com.openwebstart.os.MenuAndDesktopEntryHandler;
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

    public static void main(final String[] args) {
        Install4JUtils.applicationVersion().ifPresent(v -> LOG.info("Starting OpenWebStart {}", v));

        Translator.addBundle("i18n");

        LOG.info("OpenWebStartLauncher called with args {}.", Arrays.toString(args));
        LOG.debug("OS detected: Win[{}], MacOS[{}], Linux[{}]",
                InstallerUtil.isWindows(), InstallerUtil.isMacOS(), InstallerUtil.isLinux());

        final DeploymentConfiguration config = new DeploymentConfiguration();
        try {
            config.load();
        } catch (final ConfigurationException e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("error.loadConfig"), e);
            System.exit(-1);
        }

        try {
            new InitialConfigurationCheck(config).check();
        } catch (final Exception e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("error.initialConfig"), e);
            System.exit(-1);
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

        final List<String> bootArgs = skipNotRelevantArgs(args);
        final JavaRuntimeProvider javaRuntimeProvider = JavaRuntimeManager.getJavaRuntimeProvider(
                RuntimeDownloadDialog::showDownloadDialog,
                DialogFactory::askForRuntimeUpdate
        );

        JNLPRuntime.setForkingStrategy(ALWAYS);

        LOG.info("Calling ITW Boot with args {}.", bootArgs);
        Boot.main(new OwsJvmLauncher(javaRuntimeProvider), new MenuAndDesktopEntryHandler(), bootArgs.toArray(new String[0]));
    }

    private static List<String> skipNotRelevantArgs(final String[] args) {
        final List<String> relevantJavawsArgs = Arrays.stream(args)
                .filter(arg -> !arg.equals(CommandLineOptions.NOFORK.getOption()))
                .collect(Collectors.toList());

        LOG.debug("RelevantJavawsArgs: '{}'", relevantJavawsArgs);

        return relevantJavawsArgs;
    }
}
