package com.openwebstart.jvm.ui;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class RuntimeManagerPanelProvider implements ControlPanelProvider {

    @Override
    public String getName() {
        return "JVM Manager";
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
