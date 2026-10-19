package com.openwebstart.app.imported.ui;

import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JComponent;

public class ImportedAppsPanelProvider implements ControlPanelProvider {

    @Override
    public String getTitle() {
        return Translator.getInstance().translate("importedApps.title");
    }

    @Override
    public String getName() {
        return "ImportedAppsPanel";
    }

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public JComponent createPanel(final DeploymentConfiguration deploymentConfiguration) {
        return new ImportedAppsPanel();
    }
}
