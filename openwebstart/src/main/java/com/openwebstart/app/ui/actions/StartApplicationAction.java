package com.openwebstart.app.ui.actions;

import com.openwebstart.app.Application;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.launcher.PhaseTwoWebStartLauncher;
import com.openwebstart.ui.BasicAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.util.concurrent.Executors;

public class StartApplicationAction extends BasicAction<Application> {

    private static final Logger LOG = LoggerFactory.getLogger(StartApplicationAction.class);


    public StartApplicationAction() {
        super(Translator.getInstance().translate("appManager.action.startApplication.text"), Translator.getInstance().translate("appManager.action.startApplication.description"));
    }

    @Override
    public void call(final Application item) {
        final URL fileLocation = item.getJnlpFile().getFileLocation();

        Executors.newSingleThreadExecutor().submit(() -> {
            Thread.currentThread().setName("Starter Thread for '" + item.getName() + "'");
            LOG.info("Starting '{}'", item.getName());
            try {
                PhaseTwoWebStartLauncher.main(fileLocation.toString());
            } catch (final Exception e) {
                DialogFactory.showErrorDialog("Error in executing application '" + item.getName() + "'", e);
            }
        });

    }
}
