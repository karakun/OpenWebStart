package com.openwebstart.proxy.ui;

import com.openwebstart.controlpanel.FormPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ProxyConfigPanelDemo extends FormPanel {

    public static void main(String[] args) throws Exception {
        Translator.addBundle("i18n");

        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            frame.setContentPane(new ProxyConfigPanel(new DeploymentConfiguration()));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
