package com.openwebstart.debug;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.launcher.OwsJvmLauncher;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class DebugPanel extends FormPanel {
    final DeploymentConfiguration config;
    final Translator translator;
    final JCheckBox activateDebugCheckbox;
    final JCheckBox startSuspendedCheckbox;
    final JCheckBox anyPortCheckbox;
    final JLabel debugPortLabel;
    final JTextField debugPortField;
    final JTextArea warningLabel;
    final JTextArea messageLabel;

    private void updateControlStatus() {
        final boolean enabled = activateDebugCheckbox.isSelected();
        final boolean usingSpecificPort = !anyPortCheckbox.isSelected();

        startSuspendedCheckbox.setEnabled(enabled);
        anyPortCheckbox.setEnabled(enabled);

        debugPortLabel.setEnabled(enabled && usingSpecificPort);
        debugPortField.setEnabled(enabled && usingSpecificPort);

        warningLabel.setEnabled(enabled);

        warningLabel.setVisible(enabled && usingSpecificPort);

        messageLabel.setVisible(enabled);
    }

    private int getPortToBeUsed() throws Exception {
        final String portAsString = debugPortField.getText();
        final int port = Integer.parseInt(portAsString);
        if (port <= 0) {
            throw new Exception("Negative port number");
        }
        return port;
    }

    private void updateMessageLabel() {
        final boolean useAnyPort = anyPortCheckbox.isSelected();
        final boolean startSuspended = startSuspendedCheckbox.isSelected();

        try {
            // use the port only when needed
            final int port = useAnyPort ? 0 : getPortToBeUsed();
            String parameters = OwsJvmLauncher.getRemoteDebugParameters(useAnyPort,startSuspended,port);
            messageLabel.setText(translator.translate("debugPanel.description.success", parameters));
        } catch (final Exception ignore) {
            final String lastValidValue = config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT);

            final int port = Integer.parseInt(lastValidValue);
            String parameters = OwsJvmLauncher.getRemoteDebugParameters(useAnyPort,startSuspended,port);
            messageLabel.setText(translator.translate("debugPanel.description.fail", parameters));
        }
    }

    public DebugPanel(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "deploymentConfiguration");
        this.config = config;

        translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(config);
        int row = 0;

        JTextArea infoLabel = new JTextArea(translator.translate("debugPanel.info"));
        infoLabel.setEditable(false);
        infoLabel.setBackground(null);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        addRow(row, infoLabel);
        row++;

        activateDebugCheckbox = new JCheckBox(translator.translate("debugPanel.activateDebug.text"));
        activateDebugCheckbox.setToolTipText(translator.translate("debugPanel.activateDebug.description"));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG, activateDebugCheckbox);
        activateDebugCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG)));
        addRow(row, activateDebugCheckbox);
        row++;

        startSuspendedCheckbox = new JCheckBox(translator.translate("debugPanel.startSuspended.text"));
        startSuspendedCheckbox.setToolTipText(translator.translate("debugPanel.startSuspended.description"));
        uiLock.update(OwsDefaultsProvider.START_SUSPENDED, startSuspendedCheckbox);
        startSuspendedCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.START_SUSPENDED)));
        addRow(row, startSuspendedCheckbox);
        row++;

        anyPortCheckbox = new JCheckBox(translator.translate("debugPanel.randomPort.text"));
        anyPortCheckbox.setToolTipText(translator.translate("debugPanel.randomPort.description"));
        uiLock.update(OwsDefaultsProvider.RANDOM_DEBUG_PORT, anyPortCheckbox);
        anyPortCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.RANDOM_DEBUG_PORT)));
        addRow(row, anyPortCheckbox);
        row++;

        debugPortLabel = new JLabel(translator.translate("debugPanel.specificPort.text") + ":");
        debugPortField = new JTextField();
        debugPortField.setToolTipText(translator.translate("debugPanel.specificPort.description"));
        debugPortField.setText(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG, debugPortField);
        addRow(row, debugPortLabel, debugPortField);
        row++;

        warningLabel = new JTextArea(translator.translate("debugPanel.oneInstanceWarning"));
        warningLabel.setEditable(false);
        warningLabel.setBackground(null);
        warningLabel.setWrapStyleWord(true);
        warningLabel.setLineWrap(true);
        addRow(row, warningLabel);
        final int warningLabelRow = row;
        row++;

        addRow(row, new JPanel());
        final int panelRow = row;
        row++;

        messageLabel = new JTextArea();
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        addRow(row, messageLabel);
        row++;

        addFlexibleRow(warningLabelRow);

        updateControlStatus();
        activateDebugCheckbox.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        final boolean activated = activateDebugCheckbox.isSelected();
                        config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG, Boolean.valueOf(activated).toString());
                        updateControlStatus();
                    }
                }
        );
        anyPortCheckbox.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        final boolean useAnyPort = anyPortCheckbox.isSelected();
                        config.setProperty(OwsDefaultsProvider.RANDOM_DEBUG_PORT, Boolean.valueOf(useAnyPort).toString());
                        updateControlStatus();
                        updateMessageLabel();
                    }
                }
        );
        startSuspendedCheckbox.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        final boolean startSuspended = startSuspendedCheckbox.isSelected();
                        config.setProperty(OwsDefaultsProvider.START_SUSPENDED, Boolean.valueOf(startSuspended).toString());
                        updateMessageLabel();
                    }
                }
        );

        final Consumer<String> onPortTextUpdate = s -> {
            try {
                final int port = getPortToBeUsed();
                config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT, port + "");
            } catch (final Exception ignore) {
                // invalid port
            }
            updateMessageLabel();
        };

        debugPortField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                onPortTextUpdate.accept(debugPortField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                onPortTextUpdate.accept(debugPortField.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                onPortTextUpdate.accept(debugPortField.getText());
            }
        });

        onPortTextUpdate.accept(debugPortField.getText());

    }
}
