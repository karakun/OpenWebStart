package com.openwebstart.debug;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class DebugPanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return Translator.getInstance().translate("debugPanel.title");
    }

    @Override
    public String getName() {
        return "DebugOptions";
    }

    @Override
    public int getOrder() {
        return 31;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration deploymentConfiguration) {
        return new DebugPanel(deploymentConfiguration);
    }
}
