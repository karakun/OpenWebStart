package com.openwebstart.controlpanel;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class SimpleDesktopSettingsPanelProvider implements ControlPanelProvider {

    public static final String NAME = "SimpleDesktopSettingsPanel";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTitle() {
        return Translator.R("CPTabDesktopIntegration");
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration config) {
        return new SimpleDesktopSettingsPanel(config);
    }

}
