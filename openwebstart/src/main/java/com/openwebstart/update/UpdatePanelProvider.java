package com.openwebstart.update;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;
import java.util.Objects;

import static com.openwebstart.config.OwsDefaultsProvider.OWS_MODE;
import static com.openwebstart.config.OwsMode.EMBEDDED;

public class UpdatePanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return Translator.getInstance().translate("updatesPanel.title");
    }

    @Override
    public String getName() {
        return "UpdatePanel";
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration deploymentConfiguration) {
        return new UpdatePanel(deploymentConfiguration);
    }

    @Override
    public boolean isActive(DeploymentConfiguration config) {
        final String owsMode = config.getProperty(OWS_MODE);
        return !(Objects.equals(EMBEDDED.name(), owsMode));
    }
}
