package com.openwebstart.about;

import com.openwebstart.update.UpdatePanel;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.AboutPanel;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class OpenWebStartAboutPanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return Translator.getInstance().translate("aboutPanel.title");
    }

    @Override
    public String getName() {
        return "OpenWebStartAboutPanel";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration deploymentConfiguration) {
        return new OpenWebStartAboutPanel(deploymentConfiguration);
    }
}
