package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
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
import java.awt.GridLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.openwebstart.jvm.runtimes.Vendor.ADOPT;
import static com.openwebstart.jvm.runtimes.Vendor.AMAZON;
import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;
import static com.openwebstart.jvm.runtimes.Vendor.BELLSOFT;
import static com.openwebstart.jvm.runtimes.Vendor.ORACLE;

public class ConfigurationDialog extends JDialog {

    public ConfigurationDialog() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("JVM Manager Configuration");

        final JLabel updateStrategyLabel = new JLabel("Update strategy:");
        final JComboBox<RuntimeUpdateStrategy> updateStrategyComboBox = new JComboBox<>(RuntimeUpdateStrategy.values());
        updateStrategyComboBox.setSelectedItem(RuntimeManagerConfig.getStrategy());

        final JLabel defaultVendorLabel = new JLabel("Default vendor:");
        final JComboBox<String> defaultVendorComboBox = new JComboBox<>(new String[]{ANY_VENDOR.getName(), ADOPT.getName(), AMAZON.getName(), BELLSOFT.getName(), ORACLE.getName()});
        defaultVendorComboBox.setEditable(true);
        defaultVendorComboBox.setSelectedItem(RuntimeManagerConfig.getDefaultVendor());

        final Checkbox allowAnyVendorCheckBox = new Checkbox("Allow other vendors");
        allowAnyVendorCheckBox.setState(!RuntimeManagerConfig.isSpecificVendorEnabled());

        final JLabel defaultUpdateServerLabel = new JLabel("Default update server URL:");
        final JTextField defaultUpdateServerField = new JTextField();
        defaultUpdateServerField.setText(Optional.ofNullable(RuntimeManagerConfig.getDefaultRemoteEndpoint()).map(URI::toString).orElse(""));

        final Checkbox allowAnyUpdateServerCheckBox = new Checkbox("Allow other servers");
        allowAnyVendorCheckBox.setState(!RuntimeManagerConfig.isSpecificRemoteEndpointsEnabled());

        final JLabel supportedVersionRangeLabel = new JLabel("Supported runtime version range:");
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(VersionString::toString).orElse(""));


        final JButton okButton = new JButton("ok");
        okButton.addActionListener(e -> {
            try {
                RuntimeManagerConfig.setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultVendor((String) defaultVendorComboBox.getSelectedItem());
                RuntimeManagerConfig.setSpecificVendorEnabled(!allowAnyVendorCheckBox.getState());
                RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.setSpecificRemoteEndpointsEnabled(!allowAnyUpdateServerCheckBox.getState());
                RuntimeManagerConfig.setSupportedVersionRange(Optional.ofNullable(supportedVersionRangeField.getText()).filter(t -> !t.trim().isEmpty()).map(VersionString::fromString).orElse(null));
                close();
            } catch (URISyntaxException ex) {
                new ErrorDialog("The URI for the default update server is invalid", ex).showAndWait();
            }
        });

        final JButton cancelButton = new JButton("cancel");
        cancelButton.addActionListener(e -> close());


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

    private void close() {
        setVisible(false);
        dispose();
    }
}
