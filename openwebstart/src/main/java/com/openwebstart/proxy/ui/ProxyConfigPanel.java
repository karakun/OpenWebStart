package com.openwebstart.proxy.ui;

import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.proxy.ProxyProviderType;
import com.openwebstart.ui.Notifications;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.controlpanel.AdvancedProxySettingsDialog;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ProxyConfigPanel extends FormPanel {

    private final static Logger LOG = LoggerFactory.getLogger(ProxyConfigPanel.class);

    private final JRadioButton noProxySelection;

    private final JRadioButton useWinSystemSettings;

    private final JRadioButton useFirefoxSettings;

    private final JRadioButton usePacSettings;

    private final JRadioButton useManualSettings;

    private final JCheckBox bypassLocalhostCheckbox;

    private final JTextField pacUrlField;

    private final JTextField proxyHostField;

    private final JTextField proxyPortField;

    private final JButton advancedButton;

    private final DeploymentConfiguration config;

    public ProxyConfigPanel(final DeploymentConfiguration config) {
        this.config = Assert.requireNonNull(config, "config");

        final ButtonGroup basicConfigButtonGroup = new ButtonGroup();

        final Translator translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(config);

        final FocusListener updateConfigOnFocusLost = new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                updateConfig();
            }
        };

        noProxySelection = new JRadioButton(translator.translate("proxyPanel.noProxy.text"));
        noProxySelection.addActionListener(e -> onRadioButtonSelected(ProxyProviderType.NONE));
        noProxySelection.setToolTipText(translator.translate("proxyPanel.noProxy.description"));
        uiLock.update(ConfigurationConstants.KEY_PROXY_TYPE, noProxySelection);
        basicConfigButtonGroup.add(noProxySelection);
        addRow(0, noProxySelection);

        useWinSystemSettings = new JRadioButton(translator.translate("proxyPanel.systemSettings.text"));
        useWinSystemSettings.addActionListener(e -> onRadioButtonSelected(ProxyProviderType.OPERATION_SYSTEM));
        useWinSystemSettings.setToolTipText(translator.translate("proxyPanel.systemSettings.description"));
        uiLock.update(ConfigurationConstants.KEY_PROXY_TYPE, useWinSystemSettings);
        basicConfigButtonGroup.add(useWinSystemSettings);
        if (ProxyProviderType.OPERATION_SYSTEM.isSupported()) {
            addRow(1, useWinSystemSettings);
        }

        useFirefoxSettings = new JRadioButton(translator.translate("proxyPanel.firefoxSettings.text"));
        useFirefoxSettings.addActionListener(e -> onRadioButtonSelected(ProxyProviderType.FIREFOX));
        useFirefoxSettings.setToolTipText(translator.translate("proxyPanel.firefoxSettings.description"));
        uiLock.update(ConfigurationConstants.KEY_PROXY_TYPE, useFirefoxSettings);
        basicConfigButtonGroup.add(useFirefoxSettings);
        if (ProxyProviderType.FIREFOX.isSupported()) {
            addRow(2, useFirefoxSettings);
        }

        usePacSettings = new JRadioButton(translator.translate("proxyPanel.pacSettings.text"));
        usePacSettings.addActionListener(e -> onRadioButtonSelected(ProxyProviderType.MANUAL_PAC_URL));
        usePacSettings.setToolTipText(translator.translate("proxyPanel.pacSettings.description"));
        uiLock.update(ConfigurationConstants.KEY_PROXY_TYPE, usePacSettings);
        basicConfigButtonGroup.add(usePacSettings);
        final FormPanel pacSettingsDetailsPanel = new FormPanel();
        final JLabel pacUrlLabel = new JLabel(translator.translate("proxyPanel.pacUrl.text") + ":");
        pacUrlField = new JTextField();
        pacUrlField.addFocusListener(updateConfigOnFocusLost);
        pacUrlField.setToolTipText(translator.translate("proxyPanel.pacUrl.description"));
        pacSettingsDetailsPanel.addRow(0, pacUrlLabel, pacUrlField);
        pacSettingsDetailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 64, 0, 12));
        final JPanel pacSettingsPanel = new JPanel(new BorderLayout());
        pacSettingsPanel.add(usePacSettings, BorderLayout.NORTH);
        pacSettingsPanel.add(pacSettingsDetailsPanel, BorderLayout.CENTER);
        addRow(3, pacSettingsPanel);

        useManualSettings = new JRadioButton(translator.translate("proxyPanel.manualSettings.text"));
        useManualSettings.addActionListener(e -> onRadioButtonSelected(ProxyProviderType.MANUAL_HOSTS));
        useManualSettings.setToolTipText(translator.translate("proxyPanel.manualSettings.description"));
        uiLock.update(ConfigurationConstants.KEY_PROXY_TYPE, useManualSettings);
        basicConfigButtonGroup.add(useManualSettings);
        final JPanel manualSettingsDetailsPanel = new JPanel(new GridBagLayout());
        manualSettingsDetailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 64, 0, 12));
        final JLabel proxyHostLabel = new JLabel(translator.translate("proxyPanel.proxyAddress.text") + ":");
        final GridBagConstraints proxyHostLabelContraints = new GridBagConstraints();
        proxyHostLabelContraints.gridx = 0;
        proxyHostLabelContraints.gridy = 0;
        proxyHostLabelContraints.weightx = 0;
        proxyHostLabelContraints.weighty = 0;
        proxyHostLabelContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(proxyHostLabel, proxyHostLabelContraints);
        proxyHostField = new JTextField();
        proxyHostField.addFocusListener(updateConfigOnFocusLost);
        proxyHostField.setToolTipText(translator.translate("proxyPanel.proxyHost.description"));
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
        proxyPortField = new JTextField();
        proxyPortField.addFocusListener(updateConfigOnFocusLost);
        proxyPortField.setColumns(4);
        proxyPortField.setToolTipText(translator.translate("proxyPanel.proxyPort.description"));
        final GridBagConstraints proxyPortFieldContraints = new GridBagConstraints();
        proxyPortFieldContraints.gridx = 3;
        proxyPortFieldContraints.gridy = 0;
        proxyPortFieldContraints.weightx = 0;
        proxyPortFieldContraints.weighty = 0;
        proxyHostFieldContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(proxyPortField, proxyPortFieldContraints);
        advancedButton = new JButton(translator.translate("proxyPanel.advanced.text"));
        advancedButton.setToolTipText(translator.translate("proxyPanel.advanced.description"));
        advancedButton.addActionListener(e -> {
            AdvancedProxySettingsDialog.showAdvancedProxySettingsDialog(config);
            updateUi();
        });
        final GridBagConstraints advancedButtonContraints = new GridBagConstraints();
        advancedButtonContraints.gridx = 4;
        advancedButtonContraints.gridy = 0;
        advancedButtonContraints.weightx = 0;
        advancedButtonContraints.weighty = 0;
        advancedButtonContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(advancedButton, advancedButtonContraints);

        bypassLocalhostCheckbox = new JCheckBox(translator.translate("proxyPanel.bypassLocalhost.text"));
        bypassLocalhostCheckbox.setToolTipText(translator.translate("proxyPanel.bypassLocalhost.description"));
        final GridBagConstraints bypassLocalhostCheckboxContraints = new GridBagConstraints();
        bypassLocalhostCheckboxContraints.gridx = 0;
        bypassLocalhostCheckboxContraints.gridwidth=4;
        bypassLocalhostCheckboxContraints.gridy = 1;
        bypassLocalhostCheckboxContraints.weightx = 0;
        bypassLocalhostCheckboxContraints.weighty = 0;
        bypassLocalhostCheckboxContraints.fill = GridBagConstraints.HORIZONTAL;
        manualSettingsDetailsPanel.add(bypassLocalhostCheckbox, bypassLocalhostCheckboxContraints);


        final JPanel manualSettingsPanel = new JPanel(new BorderLayout());
        manualSettingsPanel.add(useManualSettings, BorderLayout.NORTH);
        manualSettingsPanel.add(manualSettingsDetailsPanel, BorderLayout.CENTER);
        addRow(4, manualSettingsPanel);

        addFlexibleRow(5);

        updateUi();
    }

    private void onRadioButtonSelected(final ProxyProviderType providerType) {
        updateEnabledStateForProxyType(providerType);
        updateConfig();
    }

    private void updateEnabledStateForProxyType(final ProxyProviderType providerType) {
        final boolean isPac = providerType == ProxyProviderType.MANUAL_PAC_URL;
        pacUrlField.setEnabled(isPac && !config.isLocked(ConfigurationConstants.KEY_PROXY_TYPE));

        final boolean isManual = providerType == ProxyProviderType.MANUAL_HOSTS;
        proxyHostField.setEnabled(isManual && !config.isLocked(ConfigurationConstants.KEY_PROXY_TYPE));
        proxyPortField.setEnabled(isManual && !config.isLocked(ConfigurationConstants.KEY_PROXY_TYPE));
        advancedButton.setEnabled(isManual && !config.isLocked(ConfigurationConstants.KEY_PROXY_TYPE));
        bypassLocalhostCheckbox.setEnabled(isManual && !config.isLocked(ConfigurationConstants.KEY_PROXY_TYPE));
    }

    private void updateConfig() {
        if (noProxySelection.isSelected()) {
            config.setProperty(ConfigurationConstants.KEY_PROXY_TYPE, ProxyProviderType.NONE.getConfigValue() + "");
        } else if (useFirefoxSettings.isSelected()) {
            config.setProperty(ConfigurationConstants.KEY_PROXY_TYPE, ProxyProviderType.FIREFOX.getConfigValue() + "");
        } else if (useWinSystemSettings.isSelected()) {
            config.setProperty(ConfigurationConstants.KEY_PROXY_TYPE, ProxyProviderType.OPERATION_SYSTEM.getConfigValue() + "");
        } else if (useManualSettings.isSelected()) {
            config.setProperty(ConfigurationConstants.KEY_PROXY_TYPE, ProxyProviderType.MANUAL_HOSTS.getConfigValue() + "");
        } else if (usePacSettings.isSelected()) {
            config.setProperty(ConfigurationConstants.KEY_PROXY_TYPE, ProxyProviderType.MANUAL_PAC_URL.getConfigValue() + "");
        }

        config.setProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL, pacUrlField.getText());
        config.setProperty(ConfigurationConstants.KEY_PROXY_HTTP_HOST, proxyHostField.getText());
        config.setProperty(ConfigurationConstants.KEY_PROXY_HTTP_PORT, proxyPortField.getText());
        config.setProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL, bypassLocalhostCheckbox.isSelected() + "");
    }

    private void updateUi() {
        try {
            final String proxyTypeString = config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE);
            if (proxyTypeString == null) {
                noProxySelection.setSelected(true);
                updateEnabledStateForProxyType(ProxyProviderType.NONE);
            } else {
                final int proxyTypeConfigValue = Integer.valueOf(proxyTypeString);
                final ProxyProviderType providerType = ProxyProviderType.getForConfigValue(proxyTypeConfigValue);
                if (providerType == ProxyProviderType.NONE) {
                    noProxySelection.setSelected(true);
                } else if (providerType == ProxyProviderType.FIREFOX) {
                    useFirefoxSettings.setSelected(true);
                } else if (providerType == ProxyProviderType.OPERATION_SYSTEM) {
                    useWinSystemSettings.setSelected(true);
                } else if (providerType == ProxyProviderType.MANUAL_HOSTS) {
                    useManualSettings.setSelected(true);
                } else if (providerType == ProxyProviderType.MANUAL_PAC_URL) {
                    usePacSettings.setSelected(true);
                }
                updateEnabledStateForProxyType(providerType);
            }
        } catch (final Exception e) {
            Notifications.showError(Translator.getInstance().translate("proxyPanel.error.loadSettings"));
            LOG.error("Error while loading proxy settings", e);
            noProxySelection.setSelected(true);
            updateEnabledStateForProxyType(ProxyProviderType.NONE);
        }
        pacUrlField.setText(config.getProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL));
        proxyHostField.setText(config.getProperty(ConfigurationConstants.KEY_PROXY_HTTP_HOST));
        proxyPortField.setText(config.getProperty(ConfigurationConstants.KEY_PROXY_HTTP_PORT));

        bypassLocalhostCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL)));

    }
}
