package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeManagerConstants;
import com.openwebstart.jvm.runtimes.RuntimeUpdateStrategy;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class ConfigurationDialog extends JDialog {

    public ConfigurationDialog() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("JVM Manager Configuration");

        final JLabel updateStrategyLabel = new JLabel("Update strategy:");
        final JComboBox<RuntimeUpdateStrategy> updateStrategyComboBox = new JComboBox<>(RuntimeUpdateStrategy.values());
        updateStrategyComboBox.setSelectedItem(RuntimeManagerConfig.getInstance().getStrategy());

        final JLabel defaultVendorLabel = new JLabel("Default vendor:");
        final JComboBox<String> defaultVendorComboBox = new JComboBox<>(new String[]{RuntimeManagerConstants.VENDOR_ANY, RuntimeManagerConstants.VENDOR_ADOPT, RuntimeManagerConstants.VENDOR_AMAZON, RuntimeManagerConstants.VENDOR_BELLSOFT, RuntimeManagerConstants.VENDOR_ORACLE});
        defaultVendorComboBox.setEditable(true);
        defaultVendorComboBox.setSelectedItem(RuntimeManagerConfig.getInstance().getDefaultVendor());

        final Checkbox allowAnyVendorCheckBox = new Checkbox("Allow other vendors");
        allowAnyVendorCheckBox.setState(!RuntimeManagerConfig.getInstance().isSpecificVendorEnabled());

        final JLabel defaultUpdateServerLabel = new JLabel("Default update server URL:");
        final JTextField defaultUpdateServerField = new JTextField();
        defaultUpdateServerField.setText(Optional.ofNullable(RuntimeManagerConfig.getInstance().getDefaultRemoteEndpoint()).map(u -> u.toString()).orElse(""));

        final Checkbox allowAnyUpdateServerCheckBox = new Checkbox("Allow other servers");
        allowAnyVendorCheckBox.setState(!RuntimeManagerConfig.getInstance().isSpecificRemoteEndpointsEnabled());

        final JLabel supportedVersionRangeLabel = new JLabel("Supported runtime version range:");
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getInstance().getSupportedVersionRange()).map(u -> u.toString()).orElse(""));


        final JButton okButton = new JButton("ok");
        okButton.addActionListener(e -> {
            try {
                RuntimeManagerConfig.getInstance().setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.getInstance().setDefaultVendor((String) defaultVendorComboBox.getSelectedItem());
                RuntimeManagerConfig.getInstance().setSpecificVendorEnabled(!allowAnyVendorCheckBox.getState());
                RuntimeManagerConfig.getInstance().setDefaultRemoteEndpoint(new URI(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.getInstance().setSpecificRemoteEndpointsEnabled(!allowAnyUpdateServerCheckBox.getState());
                RuntimeManagerConfig.getInstance().setSupportedVersionRange(Optional.ofNullable(supportedVersionRangeField.getText()).filter(t -> !t.trim().isEmpty()).map(t -> VersionString.fromString(t)).orElse(null));
                dispose();
            } catch (URISyntaxException ex) {
                new ErrorDialog("The URI for the default update server is invalid", ex).showAndWait();
            }
        });

        final JButton cancelButton = new JButton("cancel");
        cancelButton.addActionListener(e -> dispose());


        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 6, 2));
        mainPanel.add(updateStrategyLabel);
        mainPanel.add(updateStrategyComboBox);
        mainPanel.add(defaultVendorLabel);
        mainPanel.add(defaultVendorComboBox);
        mainPanel.add(new JPanel());
        mainPanel.add(allowAnyVendorCheckBox);
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

    public void showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
