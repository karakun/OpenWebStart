package com.openwebstart.debug;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.controlpanel.FormPanel;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class DebugPanel extends FormPanel {

    public DebugPanel(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "deploymentConfiguration");

        final Translator translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(config);

        final JTextArea infoLabel = new JTextArea(translator.translate("debugPanel.info"));
        infoLabel.setEditable(false);
        infoLabel.setBackground(null);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        addRow(0, infoLabel);


        final JLabel debugPortLabel = new JLabel(translator.translate("debugPanel.debugPort.text") + ":");
        final JTextField debugPortField = new JTextField();
        debugPortField.setToolTipText(translator.translate("debugPanel.debugPort.description"));
        debugPortField.setText(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG, debugPortField);
        addRow(1, debugPortLabel, debugPortField);

        final JCheckBox activateDebugCheckbox = new JCheckBox(translator.translate("debugPanel.activateDebug.text"));
        activateDebugCheckbox.setToolTipText(translator.translate("debugPanel.activateDebug.description"));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG, activateDebugCheckbox);
        activateDebugCheckbox.addChangeListener(e -> config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG, Boolean.valueOf(activateDebugCheckbox.isSelected()).toString()));
        activateDebugCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG)));
        addEditorRow(2, activateDebugCheckbox);

        addRow(3, new JPanel());

        final JTextArea messageLabel = new JTextArea();
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        addRow(4, messageLabel);

        addFlexibleRow(5);


        final Consumer<String> onPortTextUpdate = s -> {
            try {
                final int port = Integer.parseInt(debugPortField.getText());
                config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT, port + "");
                messageLabel.setText(translator.translate("debugPanel.description.success", Integer.toString(port)));
            } catch (final Exception ignore) {
                final String lastValidValue = config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT);
                messageLabel.setText(translator.translate("debugPanel.description.fail", lastValidValue));
            }
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
