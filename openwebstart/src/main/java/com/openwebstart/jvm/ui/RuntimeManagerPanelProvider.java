package com.openwebstart.jvm.ui;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class RuntimeManagerPanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return "JVM Manager";
    }

    @Override
    public String getName() {
        return "RuntimeManagerPanel";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration deploymentConfiguration) {
        return new RuntimeManagerPanel(deploymentConfiguration);
    }
}
