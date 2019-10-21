package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import com.openwebstart.jvm.ui.util.TranslatableEnumComboboxRenderer;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.awt.Cursor.WAIT_CURSOR;
import static java.awt.Cursor.getDefaultCursor;
import static java.awt.Cursor.getPredefinedCursor;

public class ConfigurationDialog extends ModalDialog {
    private static final Color ERROR_BACKGROUND = Color.yellow;

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor() ;
    private final Translator translator = Translator.getInstance();
    private final JComboBox vendorComboBox;
    private final Color originalBackground;
    private boolean urlValidationError = false;

    public ConfigurationDialog() {
        final Translator translator = Translator.getInstance();

        setTitle(translator.translate("dialog.jvmManagerConfig.title"));

        final JLabel updateStrategyLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.updateStrategy.text"));
        final JComboBox<RuntimeUpdateStrategy> updateStrategyComboBox = new JComboBox<>(RuntimeUpdateStrategy.values());
        updateStrategyComboBox.setRenderer(new TranslatableEnumComboboxRenderer<>());
        updateStrategyComboBox.setSelectedItem(RuntimeManagerConfig.getStrategy());

        final JLabel defaultVendorLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.vendor.text"));
        vendorComboBox = new JComboBox();
        backgroundExecutor.execute(() -> updateVendorComboBox(RuntimeManagerConfig.getDefaultRemoteEndpoint()));
        vendorComboBox.setEditable(true);


        final JLabel defaultUpdateServerLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.defaultServerUrl.text"));
        final JTextField defaultUpdateServerField = new JTextField();
        originalBackground = defaultUpdateServerField.getBackground();
        defaultUpdateServerField.setText(Optional.ofNullable(RuntimeManagerConfig.getDefaultRemoteEndpoint()).map(URL::toString).orElse(""));
        defaultUpdateServerField.addFocusListener(new MyFocusAdapter());

        final JCheckBox allowAnyUpdateServerCheckBox = new JCheckBox(translator.translate("dialog.jvmManagerConfig.allowServerInJnlp.text"));
        allowAnyUpdateServerCheckBox.setSelected(RuntimeManagerConfig.isNonDefaultServerAllowed());

        final JLabel supportedVersionRangeLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.versionRange.text"));
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(VersionString::toString).orElse(""));

        final JButton okButton = new JButton(translator.translate("action.ok"));
        okButton.addActionListener(e -> {
            try {
                if (urlValidationError) {
                    defaultUpdateServerField.requestFocus();
                    return;
                }
                RuntimeManagerConfig.setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultVendor((String) vendorComboBox.getSelectedItem());
                // TODO : Why URI when we are doing HttpRequest on the endpoint?
                RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.setNonDefaultServerAllowed(allowAnyUpdateServerCheckBox.isSelected());
                RuntimeManagerConfig.setSupportedVersionRange(Optional.ofNullable(supportedVersionRangeField.getText()).filter(t -> !t.trim().isEmpty()).map(VersionString::fromString).orElse(null));
                close();
            } catch (final URISyntaxException ex) {
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

    private void updateVendorComboBox(final URL specifiedServerURL) {
        try {
            SwingUtilities.invokeLater(() -> vendorComboBox.setCursor(getPredefinedCursor(WAIT_CURSOR)));
            final String[] vendorNames = JavaRuntimeManager.getAllVendors(specifiedServerURL);
            SwingUtilities.invokeLater(() -> {
                vendorComboBox.setModel(new DefaultComboBoxModel(vendorNames));
                vendorComboBox.setSelectedItem(RuntimeManagerConfig.getVendor());
            });
        } catch (final Exception ex) {
            DialogFactory.showErrorDialog(translator.translate("jvmManager.error.updateVendorNames"), ex);
        } finally {
            SwingUtilities.invokeLater(() -> vendorComboBox.setCursor(getDefaultCursor()));
        }
    }

    private class MyFocusAdapter extends FocusAdapter {
        @Override
        public void focusLost(final FocusEvent e) {
            final JTextField field = (JTextField) e.getSource();
            try {
                final URL url = new URL(field.getText());
                field.setBackground(originalBackground);
                urlValidationError = false;
                backgroundExecutor.execute(() -> updateVendorComboBox(url));
            } catch (final MalformedURLException exception) {
                field.setBackground(ERROR_BACKGROUND);
                urlValidationError = true;
                field.requestFocus();
            }
        }
    }
}
