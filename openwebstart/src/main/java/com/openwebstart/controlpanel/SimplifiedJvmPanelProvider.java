package com.openwebstart.controlpanel;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class SimplifiedJvmPanelProvider implements ControlPanelProvider {

    public static final String NAME = "SimplifiedJVMPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPTabJVMSettings");
    }

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new SimplifiedJvmPanel(config);
    }
}
