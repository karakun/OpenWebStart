package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import com.openwebstart.ui.TranslatableEnumComboboxRenderer;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class ConfigurationDialog extends ModalDialog {

    public ConfigurationDialog() {
        final Translator translator = Translator.getInstance();

        setTitle(translator.translate("dialog.jvmManagerConfig.title"));

        final JLabel updateStrategyLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.updateStrategy.text"));
        final JComboBox<RuntimeUpdateStrategy> updateStrategyComboBox = new JComboBox<>(RuntimeUpdateStrategy.values());
        updateStrategyComboBox.setRenderer(new TranslatableEnumComboboxRenderer<>());
        updateStrategyComboBox.setSelectedItem(RuntimeManagerConfig.getStrategy());

        final JLabel defaultVendorLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.vendor.text"));
        final String[] combinedList = JavaRuntimeManager.getAllVendors(RuntimeManagerConfig.getDefaultRemoteEndpoint());
        final JComboBox<String> vendorComboBox = new JComboBox<>(combinedList);
        vendorComboBox.setEditable(true);
        vendorComboBox.setSelectedItem(RuntimeManagerConfig.getVendor());

        final JLabel defaultUpdateServerLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.defaultServerUrl.text"));
        final JTextField defaultUpdateServerField = new JTextField();
        defaultUpdateServerField.setText(Optional.ofNullable(RuntimeManagerConfig.getDefaultRemoteEndpoint()).map(URL::toString).orElse(""));

        final JCheckBox allowAnyUpdateServerCheckBox = new JCheckBox(translator.translate("dialog.jvmManagerConfig.allowServerInJnlp.text"));
        allowAnyUpdateServerCheckBox.setSelected(RuntimeManagerConfig.isNonDefaultServerAllowed());


        final JLabel supportedVersionRangeLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.versionRange.text"));
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(VersionString::toString).orElse(""));

        final JButton okButton = new JButton(translator.translate("action.ok"));
        okButton.addActionListener(e -> {
            try {
                RuntimeManagerConfig.setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultVendor((String) vendorComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.setNonDefaultServerAllowed(allowAnyUpdateServerCheckBox.isSelected());
                RuntimeManagerConfig.setSupportedVersionRange(Optional.ofNullable(supportedVersionRangeField.getText()).filter(t -> !t.trim().isEmpty()).map(VersionString::fromString).orElse(null));
                close();
            } catch (URISyntaxException ex) {
                DialogFactory.showErrorDialog(translator.translate("jvmManager.error.invalidServerUri"), ex);
            }
        });

        final JButton cancelButton = new JButton(translator.translate("action.cancel"));
        cancelButton.addActionListener(e -> close());


        final FormPanel mainPanel = new FormPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        mainPanel.addRow(0, updateStrategyLabel, updateStrategyComboBox);
        mainPanel.addRow(1, defaultVendorLabel, vendorComboBox);
        mainPanel.addRow(2, defaultUpdateServerLabel, defaultUpdateServerField);
        mainPanel.addEditorRow(3, allowAnyUpdateServerCheckBox);
        mainPanel.addRow(4, supportedVersionRangeLabel, supportedVersionRangeField);
        mainPanel.addFlexibleRow(5);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(ButtonPanelFactory.createButtonPanel(okButton, cancelButton), BorderLayout.SOUTH);

        add(panel);
    }

}
