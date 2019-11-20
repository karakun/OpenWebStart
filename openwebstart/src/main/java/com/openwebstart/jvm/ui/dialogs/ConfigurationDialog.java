package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import com.openwebstart.ui.ModalDialog;
import com.openwebstart.ui.TranslatableEnumComboboxRenderer;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_DOWNLOAD_SERVER_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_VENDOR;
import static java.awt.Cursor.WAIT_CURSOR;
import static java.awt.Cursor.getDefaultCursor;
import static java.awt.Cursor.getPredefinedCursor;

public class ConfigurationDialog extends ModalDialog {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationDialog.class);

    private static final Color ERROR_BACKGROUND = Color.yellow;

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor() ;
    private final Translator translator = Translator.getInstance();
    private final JComboBox<String> vendorComboBox;
    private final Color originalBackground;
    private boolean urlValidationError = false;

    public ConfigurationDialog(final DeploymentConfiguration deploymentConfiguration) {
        setTitle(translator.translate("dialog.jvmManagerConfig.title"));

        final UiLock uiLock = new UiLock(deploymentConfiguration);

        final JLabel updateStrategyLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.updateStrategy.text"));
        final JComboBox<RuntimeUpdateStrategy> updateStrategyComboBox = new JComboBox<>(RuntimeUpdateStrategy.values());
        updateStrategyComboBox.setRenderer(new TranslatableEnumComboboxRenderer<>());
        updateStrategyComboBox.setSelectedItem(RuntimeManagerConfig.getStrategy());
        uiLock.update(JVM_UPDATE_STRATEGY, updateStrategyComboBox);

        final JLabel defaultVendorLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.vendor.text"));
        vendorComboBox = new JComboBox<>();
        backgroundExecutor.execute(() -> updateVendorComboBox(RuntimeManagerConfig.getDefaultRemoteEndpoint()));
        vendorComboBox.setEditable(true);
        uiLock.update(JVM_VENDOR, vendorComboBox);

        final JLabel defaultUpdateServerLabel = new JLabel(translator.translate("dialog.jvmManagerConfig.defaultServerUrl.text"));
        final JTextField defaultUpdateServerField = new JTextField();
        originalBackground = defaultUpdateServerField.getBackground();
        try {
            defaultUpdateServerField.setText(Optional.ofNullable(RuntimeManagerConfig.getDefaultRemoteEndpoint()).map(URL::toString).orElse(""));
        } catch (final Exception e) {
            LOG.error("Can not set default server url!", e);
        }
        defaultUpdateServerField.addFocusListener(new MyFocusAdapter());
        uiLock.update(DEFAULT_JVM_DOWNLOAD_SERVER, defaultUpdateServerField);


        final JCheckBox allowAnyUpdateServerCheckBox = new JCheckBox(translator.translate("dialog.jvmManagerConfig.allowServerInJnlp.text"));
        allowAnyUpdateServerCheckBox.setSelected(RuntimeManagerConfig.isNonDefaultServerAllowed());
        uiLock.update(ALLOW_DOWNLOAD_SERVER_FROM_JNLP, allowAnyUpdateServerCheckBox);

        final JButton okButton = new JButton(translator.translate("action.ok"));
        okButton.addActionListener(e -> {
            try {
                if (urlValidationError) {
                    defaultUpdateServerField.requestFocus();
                    return;
                }
                RuntimeManagerConfig.setStrategy((RuntimeUpdateStrategy) updateStrategyComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultVendor((String) vendorComboBox.getSelectedItem());
                RuntimeManagerConfig.setDefaultRemoteEndpoint(new URL(defaultUpdateServerField.getText()));
                RuntimeManagerConfig.setNonDefaultServerAllowed(allowAnyUpdateServerCheckBox.isSelected());
                close();
            } catch (final MalformedURLException ex) {
                DialogFactory.showErrorDialog(translator.translate("jvmManager.error.invalidServerUri"), ex);
            }
        });

        final JButton cancelButton = new JButton(translator.translate("action.cancel"));
        cancelButton.addActionListener(e -> close());

        final FormPanel mainPanel = new FormPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        mainPanel.addRow(0, updateStrategyLabel, updateStrategyComboBox);
        mainPanel.addRow(1, defaultUpdateServerLabel, defaultUpdateServerField);
        mainPanel.addEditorRow(2, allowAnyUpdateServerCheckBox);
        mainPanel.addRow(3, defaultVendorLabel, vendorComboBox);
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
            final List<String> vendorNamesList = new ArrayList<>(Arrays.asList(JavaRuntimeManager.getAllVendors(specifiedServerURL)));
            if (!vendorNamesList.contains(RuntimeManagerConfig.getVendor())) {
                vendorNamesList.add(RuntimeManagerConfig.getVendor());
            }
            SwingUtilities.invokeLater(() -> {
                vendorComboBox.setModel(new DefaultComboBoxModel<>(vendorNamesList.toArray(new String[0])));
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
