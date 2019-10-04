package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import com.openwebstart.jvm.ui.util.TranslatableEnumComboboxRenderer;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class ConfigurationDialog extends ModalDialog {

    public ConfigurationDialog() {
       setTitle("JVM Manager Configuration");

        final JLabel updateStrategyLabel = new JLabel("Update strategy:");
        final JComboBox<RuntimeUpdateStrategy> updateStrategyComboBox = new JComboBox<>(RuntimeUpdateStrategy.values());
        updateStrategyComboBox.setRenderer(new TranslatableEnumComboboxRenderer<>());
        updateStrategyComboBox.setSelectedItem(RuntimeManagerConfig.getStrategy());

        final JLabel defaultVendorLabel = new JLabel("Vendor:");
        final String[] combinedList = JavaRuntimeManager.getAllVendors(RuntimeManagerConfig.getDefaultRemoteEndpoint());
        final JComboBox<String> vendorComboBox = new JComboBox<>(combinedList);
        vendorComboBox.setEditable(true);
        vendorComboBox.setSelectedItem(RuntimeManagerConfig.getVendor());

        final JLabel defaultUpdateServerLabel = new JLabel("Default update server URL:");
        final JTextField defaultUpdateServerField = new JTextField();
        defaultUpdateServerField.setText(Optional.ofNullable(RuntimeManagerConfig.getDefaultRemoteEndpoint()).map(URL::toString).orElse(""));

        final JCheckBox allowAnyUpdateServerCheckBox = new JCheckBox("Allow server from JNLP file");
        allowAnyUpdateServerCheckBox.setSelected(RuntimeManagerConfig.isNonDefaultServerAllowed());

        final JLabel supportedVersionRangeLabel = new JLabel("Restrict JVM version range:");
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(VersionString::toString).orElse(""));

        final JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            try {
                RuntimeManagerConfig.setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultVendor((String) vendorComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.setNonDefaultServerAllowed(allowAnyUpdateServerCheckBox.isSelected());
                RuntimeManagerConfig.setSupportedVersionRange(Optional.ofNullable(supportedVersionRangeField.getText()).filter(t -> !t.trim().isEmpty()).map(VersionString::fromString).orElse(null));
                close();
            } catch (URISyntaxException ex) {
                DialogFactory.showErrorDialog("The URI for the default update server is invalid", ex);
            }
        });

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> close());


        final FormPanel mainPanel = new FormPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));


        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx=0;
        constraints.gridy=0;

        mainPanel.addRow(0, updateStrategyLabel, updateStrategyComboBox);
        mainPanel.addRow(1, defaultVendorLabel, vendorComboBox);
        mainPanel.addRow(2, defaultUpdateServerLabel, defaultUpdateServerField);
        mainPanel.addEditorRow(3, allowAnyUpdateServerCheckBox);
        mainPanel.addRow(4, supportedVersionRangeLabel, supportedVersionRangeField);
        mainPanel.addFlexibleRow(5);

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setBackground(Color.WHITE);
        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        actionWrapperPanel.add(Box.createHorizontalGlue());
        actionWrapperPanel.add(okButton);
        actionWrapperPanel.add(cancelButton);

        final JPanel topBorder = new JPanel();
        topBorder.setBackground(Color.GRAY);
        topBorder.setPreferredSize(new Dimension(1, 1));
        topBorder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        topBorder.setMinimumSize(new Dimension(1, 1));

        final JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.add(topBorder, BorderLayout.NORTH);
        actionPanel.add(actionWrapperPanel, BorderLayout.CENTER);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(ButtonPanelFactory.createButtonPanel(okButton, cancelButton), BorderLayout.SOUTH);

        add(panel);
    }

}
