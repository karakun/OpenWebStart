package com.openwebstart.launcher;

import com.openwebstart.jvm.ui.dialogs.ErrorDialog;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanel;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.naming.ConfigurationException;
import javax.swing.UIManager;

public class ControlPanelLauncher {

    public static void main(String[] args) {
        final DeploymentConfiguration config = new DeploymentConfiguration();
        try {
            config.load();
        } catch (ConfigurationException e) {
            SwingUtils.invokeAndWait(() -> {
                ErrorDialog errorDialog = new ErrorDialog("Can not load configuration", e);
                errorDialog.showAndWait();
            });
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
