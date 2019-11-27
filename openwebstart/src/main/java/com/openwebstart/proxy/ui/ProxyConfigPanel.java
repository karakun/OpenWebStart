package com.openwebstart.proxy.ui;

import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.proxy.ProxyProviderTypes;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class ProxyConfigPanel extends FormPanel {


    private final JRadioButton noProxySelection;

    private final JRadioButton useWinSystemSettings;

    private final JRadioButton useFirefoxSettings;

    private final JRadioButton usePacSettings;

    private final JRadioButton useManualSettings;

    private final DeploymentConfiguration config;

    public ProxyConfigPanel(final DeploymentConfiguration config) {
        this.config = Assert.requireNonNull(config, "config");

        final ButtonGroup basicConfigButtonGroup = new ButtonGroup();

        final Translator translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(config);

        noProxySelection = new JRadioButton(translator.translate("proxyPanel.noProxy.text"));
        noProxySelection.setToolTipText(translator.translate("proxyPanel.noProxy.description"));
        basicConfigButtonGroup.add(noProxySelection);
        addRow(0, noProxySelection);

        useWinSystemSettings = new JRadioButton(translator.translate("proxyPanel.systemSettings.text"));
        useWinSystemSettings.setToolTipText(translator.translate("proxyPanel.systemSettings.description"));
        basicConfigButtonGroup.add(useWinSystemSettings);
        if (ProxyProviderTypes.WINDOWS.isSupported()) {
            addRow(1, useWinSystemSettings);
        }

        useFirefoxSettings = new JRadioButton(translator.translate("proxyPanel.firefoxSettings.text"));
        useFirefoxSettings.setToolTipText(translator.translate("proxyPanel.firefoxSettings.description"));
        basicConfigButtonGroup.add(useFirefoxSettings);
        if (ProxyProviderTypes.FIREFOX.isSupported()) {
            addRow(2, useFirefoxSettings);
        }

        usePacSettings = new JRadioButton(translator.translate("proxyPanel.pacSettings.text"));
        usePacSettings.setToolTipText(translator.translate("proxyPanel.pacSettings.description"));
        basicConfigButtonGroup.add(usePacSettings);
        final FormPanel pacSettingsDetailsPanel = new FormPanel();
        final JLabel pacUrlLabel = new JLabel(translator.translate("proxyPanel.pacUrl.text") + ":");
        final JTextField pacUrlField = new JTextField();
        pacUrlField.setToolTipText(translator.translate("proxyPanel.pacUrl.description"));
        pacSettingsDetailsPanel.addRow(0, pacUrlLabel, pacUrlField);
        pacSettingsDetailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 64, 0, 12));
        final JPanel pacSettingsPanel = new JPanel(new BorderLayout());
        pacSettingsPanel.add(usePacSettings, BorderLayout.NORTH);
        pacSettingsPanel.add(pacSettingsDetailsPanel, BorderLayout.CENTER);
        addRow(3, pacSettingsPanel);

        useManualSettings = new JRadioButton(translator.translate("proxyPanel.manualSettings.text"));
        useManualSettings.setToolTipText(translator.translate("proxyPanel.manualSettings.description"));
        basicConfigButtonGroup.add(useManualSettings);
        final JPanel manualSettingsDetailsPanel = new JPanel(new GridBagLayout());
        manualSettingsDetailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 64, 0, 12));
        final JLabel proxyHostLabel = new JLabel(translator.translate("proxyPanel.proxyHost.text") + ":");
        final GridBagConstraints proxyHostLabelContraints = new GridBagConstraints();
        proxyHostLabelContraints.gridx = 0;
        proxyHostLabelContraints.gridy = 0;
        proxyHostLabelContraints.weightx = 0;
        proxyHostLabelContraints.weighty = 0;
        proxyHostLabelContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(proxyHostLabel, proxyHostLabelContraints);
        final JTextField proxyHostField = new JTextField();
        proxyHostField.setColumns(32);
        proxyHostField.setToolTipText(translator.translate("proxyPanel.proxyHost.description"));
        final GridBagConstraints proxyHostFieldContraints = new GridBagConstraints();
        proxyHostFieldContraints.gridx = 1;
        proxyHostFieldContraints.gridy = 0;
        proxyHostFieldContraints.weightx = 1;
        proxyHostFieldContraints.weighty = 0;
        proxyHostFieldContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(proxyHostField, proxyHostFieldContraints);
        final JLabel colonLabel = new JLabel(":");
        final GridBagConstraints colonLabelContraints = new GridBagConstraints();
        colonLabelContraints.gridx = 2;
        colonLabelContraints.gridy = 0;
        colonLabelContraints.weightx = 0;
        colonLabelContraints.weighty = 0;
        proxyHostFieldContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(colonLabel, colonLabelContraints);
        final JTextField proxyPortField = new JTextField();
        proxyPortField.setColumns(8);
        proxyPortField.setToolTipText(translator.translate("proxyPanel.proxyPort.description"));
        final GridBagConstraints proxyPortFieldContraints = new GridBagConstraints();
        proxyPortFieldContraints.gridx = 3;
        proxyPortFieldContraints.gridy = 0;
        proxyPortFieldContraints.weightx = 0.2;
        proxyPortFieldContraints.weighty = 0;
        proxyHostFieldContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(proxyPortField, proxyPortFieldContraints);
        final JButton advancedButton = new JButton(translator.translate("proxyPanel.advanced.text"));
        advancedButton.setToolTipText(translator.translate("proxyPanel.advanced.description"));
        final GridBagConstraints advancedButtonContraints = new GridBagConstraints();
        advancedButtonContraints.gridx = 4;
        advancedButtonContraints.gridy = 0;
        advancedButtonContraints.weightx = 0;
        advancedButtonContraints.weighty = 0;
        advancedButtonContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(advancedButton, advancedButtonContraints);
        final JPanel manualSettingsPanel = new JPanel(new BorderLayout());
        manualSettingsPanel.add(useManualSettings, BorderLayout.NORTH);
        manualSettingsPanel.add(manualSettingsDetailsPanel, BorderLayout.CENTER);
        addRow(4, manualSettingsPanel);

        addFlexibleRow(5);
    }

    private void updateConfig() {

    }

    private void updateUi() {
        final String proxyTypeString = config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE);
        final int proxyTypeConfigValue = Integer.valueOf(proxyTypeString);
        final ProxyProviderTypes providerType = ProxyProviderTypes.getForConfigValue(proxyTypeConfigValue);
        if (providerType == ProxyProviderTypes.NONE) {
            noProxySelection.setSelected(true);
        } else if (providerType == ProxyProviderTypes.FIREFOX) {
            useFirefoxSettings.setSelected(true);
        } else if (providerType == ProxyProviderTypes.WINDOWS) {
            useWinSystemSettings.setSelected(true);
        } else if (providerType == ProxyProviderTypes.MANUAL_HOSTS) {
            useManualSettings.setSelected(true);
        } else if (providerType == ProxyProviderTypes.MANUAL_PAC_URL) {
            usePacSettings.setSelected(true);
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            frame.setContentPane(new ProxyConfigPanel(new DeploymentConfiguration()));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
