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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GridLayout;
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
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor() ;
    private final Translator translator = Translator.getInstance();
    private final JComboBox vendorComboBox;
    private final Color originalBackground;
    private final Color errorIndicator = Color.yellow;

    public ConfigurationDialog() {
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
        final Checkbox allowAnyUpdateServerCheckBox = new Checkbox(translator.translate("dialog.jvmManagerConfig.allowServerInJnlp.text"));
        allowAnyUpdateServerCheckBox.setState(RuntimeManagerConfig.isNonDefaultServerAllowed());

        final JLabel supportedVersionRangeLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.versionRange.text"));
        final JTextField supportedVersionRangeField = new JTextField();
        supportedVersionRangeField.setText(Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(VersionString::toString).orElse(""));

        final JButton okButton = new JButton(translator.translate("action.ok"));
        okButton.addActionListener(e -> {
            try {
                if (defaultUpdateServerField.getBackground() == errorIndicator) {
                    defaultUpdateServerField.requestFocus();
                    return;
                }
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
        mainPanel.setLayout(new GridLayout(0, 2, 6, 6));
        mainPanel.add(updateStrategyLabel);
        mainPanel.add(updateStrategyComboBox);
        mainPanel.add(defaultUpdateServerLabel);
        mainPanel.add(defaultUpdateServerField);
        mainPanel.add(new JLabel());
        mainPanel.add(allowAnyUpdateServerCheckBox);
        mainPanel.add(defaultVendorLabel);
        mainPanel.add(vendorComboBox);
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

    private void updateVendorComboBox(URL specifiedServerURL) {
        try {
            SwingUtilities.invokeLater(() -> vendorComboBox.setCursor(getPredefinedCursor(WAIT_CURSOR)));
            final String[] vendorNames = JavaRuntimeManager.getAllVendors(specifiedServerURL);
            SwingUtilities.invokeLater(() -> {
                vendorComboBox.setModel(new DefaultComboBoxModel(vendorNames));
                vendorComboBox.setSelectedItem(RuntimeManagerConfig.getVendor());
            });
        } catch (Exception ex) {
            DialogFactory.showErrorDialog(translator.translate("jvmManager.error.updateVendorNames"), ex);
        } finally {
            SwingUtilities.invokeLater(() -> vendorComboBox.setCursor(getDefaultCursor()));
        }
    }

    private static URL getUrl(String text) {
        try {
            return new URL(text);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private class MyFocusAdapter extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            final JTextField field = (JTextField) e.getSource();
            final URL url = getUrl(field.getText());
            if (url == null) {
                field.setBackground(errorIndicator);
                field.requestFocus();
            } else {
                field.setBackground(originalBackground);
                backgroundExecutor.execute(() -> updateVendorComboBox(url));
            }
        }
    }
}
