package com.openwebstart.launcher;

import com.openwebstart.i18n.TranslatorInitialization;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.naming.ConfigurationException;
import javax.swing.UIManager;

public class ControlPanelLauncher {

    public static void main(final String[] args) {
        TranslatorInitialization.init();

        final DeploymentConfiguration config = new DeploymentConfiguration();
        try {
            config.load();
        } catch (final ConfigurationException e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("error.loadConfig"), e);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtils.invokeLater(() -> {
            final ControlPanel editor = new ControlPanel(config, new OpenWebStartControlPanelStyle());
            editor.setVisible(true);
        });
    }
}
