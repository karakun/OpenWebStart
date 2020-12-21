package com.openwebstart.jvm.ui;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;
import java.util.Objects;

import static com.openwebstart.config.OwsDefaultsProvider.OWS_MODE;
import static com.openwebstart.config.OwsMode.EMBEDDED;

public class RuntimeManagerPanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return Translator.getInstance().translate("jvmManager.name");
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

    @Override
    public boolean isActive(DeploymentConfiguration config) {
        final String owsMode = config.getProperty(OWS_MODE);
        return !(Objects.equals(EMBEDDED.name(), owsMode));
    }
}
