package com.openwebstart.app.ui;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class ApplicationManagerPanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return Translator.getInstance().translate("appManager.name");
    }

    @Override
    public String getName() {
        return "ApplicationManagerPanel";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean isActive(final DeploymentConfiguration config) {
        return false;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new ApplicationManagerPanel(config);
    }
}
