package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import com.openwebstart.jvm.ui.util.TranslatableEnumComboboxRenderer;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.GridLayout;
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

        final Checkbox allowAnyUpdateServerCheckBox = new Checkbox(translator.translate("dialog.jvmManagerConfig.allowServerInJnlp.text"));
        allowAnyUpdateServerCheckBox.setState(RuntimeManagerConfig.isNonDefaultServerAllowed());

        final JLabel supportedVersionRangeLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.versionRange.text"));
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(VersionString::toString).orElse(""));

        final JButton okButton = new JButton(translator.translate("action.ok"));
        okButton.addActionListener(e -> {
            try {
                RuntimeManagerConfig.setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultVendor((String) vendorComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.setNonDefaultServerAllowed(allowAnyUpdateServerCheckBox.getState());
                RuntimeManagerConfig.setSupportedVersionRange(Optional.ofNullable(supportedVersionRangeField.getText()).filter(t -> !t.trim().isEmpty()).map(VersionString::fromString).orElse(null));
                close();
            } catch (URISyntaxException ex) {
                DialogFactory.showErrorDialog(translator.translate("jvmManager.error.invalidServerUri"), ex);
            }
        });

        final JButton cancelButton = new JButton(translator.translate("action.cancel"));
        cancelButton.addActionListener(e -> close());


        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 6, 2));
        mainPanel.add(updateStrategyLabel);
        mainPanel.add(updateStrategyComboBox);
        mainPanel.add(defaultVendorLabel);
        mainPanel.add(vendorComboBox);
        mainPanel.add(defaultUpdateServerLabel);
        mainPanel.add(defaultUpdateServerField);
        mainPanel.add(new JPanel());
        mainPanel.add(allowAnyUpdateServerCheckBox);
        mainPanel.add(supportedVersionRangeLabel);
        mainPanel.add(supportedVersionRangeField);

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionWrapperPanel.add(Box.createHorizontalGlue());
        actionWrapperPanel.add(okButton);
        actionWrapperPanel.add(cancelButton);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(actionWrapperPanel, BorderLayout.SOUTH);

        add(panel);
    }

}
