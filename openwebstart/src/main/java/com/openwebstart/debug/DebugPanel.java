package com.openwebstart.debug;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.controlpanel.FormPanel;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
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

public class DebugPanel extends FormPanel {
    private final DeploymentConfiguration config;
    private final Translator translator;
    private final JCheckBox activateDebugCheckbox;
    private final JCheckBox startSuspendedCheckbox;
    private final JCheckBox anyPortCheckbox;
    private final JLabel debugHostLabel;
    private final JTextField debugHostField;
    private final JLabel debugPortLabel;
    private final JTextField debugPortField;
    private final JTextArea warningLabel;
    private final JTextArea messageLabel;

    public DebugPanel(final DeploymentConfiguration config) {
        this.config = Assert.requireNonNull(config, "deploymentConfiguration");

        translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(config);
        int row = 0;

        final JTextArea infoLabel = new JTextArea(translator.translate("debugPanel.info"));
        infoLabel.setEditable(false);
        infoLabel.setBackground(null);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        addRow(row++, infoLabel);

        activateDebugCheckbox = new JCheckBox(translator.translate("debugPanel.activateDebug.text"));
        activateDebugCheckbox.setToolTipText(translator.translate("debugPanel.activateDebug.description"));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG, activateDebugCheckbox);
        activateDebugCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG)));
        addRow(row++, activateDebugCheckbox);

        startSuspendedCheckbox = new JCheckBox(translator.translate("debugPanel.startSuspended.text"));
        startSuspendedCheckbox.setToolTipText(translator.translate("debugPanel.startSuspended.description"));
        uiLock.update(OwsDefaultsProvider.START_SUSPENDED, startSuspendedCheckbox);
        startSuspendedCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.START_SUSPENDED)));
        addRow(row++, startSuspendedCheckbox);

        anyPortCheckbox = new JCheckBox(translator.translate("debugPanel.randomPort.text"));
        anyPortCheckbox.setToolTipText(translator.translate("debugPanel.randomPort.description"));
        uiLock.update(OwsDefaultsProvider.RANDOM_DEBUG_PORT, anyPortCheckbox);
        anyPortCheckbox.setSelected(Boolean.parseBoolean(config.getProperty(OwsDefaultsProvider.RANDOM_DEBUG_PORT)));
        addRow(row++, anyPortCheckbox);

        debugHostLabel = new JLabel(translator.translate("debugPanel.host.text") + ":");
        debugHostField = new JTextField();
        debugHostField.setToolTipText(translator.translate("debugPanel.host.description"));
        debugHostField.setText(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_HOST));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG_HOST, debugHostField);
        addRow(row++, debugHostLabel, debugHostField);

        debugPortLabel = new JLabel(translator.translate("debugPanel.specificPort.text") + ":");
        debugPortField = new JTextField();
        debugPortField.setToolTipText(translator.translate("debugPanel.specificPort.description"));
        debugPortField.setText(config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT));
        uiLock.update(OwsDefaultsProvider.REMOTE_DEBUG, debugPortField);
        addRow(row++, debugPortLabel, debugPortField);

        messageLabel = new JTextArea();
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        addRow(row++, messageLabel);

        addRow(row++, new JPanel());

        warningLabel = new JTextArea(translator.translate("debugPanel.oneInstanceWarning"));
        warningLabel.setEditable(false);
        warningLabel.setBackground(null);
        warningLabel.setWrapStyleWord(true);
        warningLabel.setLineWrap(true);
        addRow(row++, warningLabel);

        addFlexibleRow(row);

        updateControlStatus();
        activateDebugCheckbox.addChangeListener(e -> {
                    final boolean activated = activateDebugCheckbox.isSelected();
                    config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG, Boolean.valueOf(activated).toString());
                    updateControlStatus();
                }
        );
        anyPortCheckbox.addChangeListener(e -> {
                    final boolean useAnyPort = anyPortCheckbox.isSelected();
                    config.setProperty(OwsDefaultsProvider.RANDOM_DEBUG_PORT, Boolean.valueOf(useAnyPort).toString());
                    updateControlStatus();
                    updateMessageLabel();
                }
        );
        startSuspendedCheckbox.addChangeListener(e -> {
                    final boolean startSuspended = startSuspendedCheckbox.isSelected();
                    config.setProperty(OwsDefaultsProvider.START_SUSPENDED, Boolean.valueOf(startSuspended).toString());
                    updateMessageLabel();
                }
        );

        debugPortField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateSpecificPort();
            }

            public void removeUpdate(DocumentEvent e) {
                updateSpecificPort();
            }

            public void insertUpdate(DocumentEvent e) {
                updateSpecificPort();
            }
        });

        debugHostField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateHost();
            }

            public void removeUpdate(DocumentEvent e) {
                updateHost();
            }

            public void insertUpdate(DocumentEvent e) {
                updateHost();
            }
        });
        updateHost();
        updateSpecificPort();
    }

    private void updateSpecificPort() {
        try {
            final int port = getPortToBeUsed();
            config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT, port + "");
        } catch (final Exception ignore) {
            // invalid port
        }
        updateMessageLabel();
    }

    private void updateHost() {
        try {
            final String host = getHostToBeUsed();
            config.setProperty(OwsDefaultsProvider.REMOTE_DEBUG_HOST, host);
        } catch (final Exception ignore) {
            // invalid host
        }
       updateMessageLabel();
    }

    private void updateControlStatus() {
        final boolean enabled = activateDebugCheckbox.isSelected();
        final boolean usingSpecificPort = !anyPortCheckbox.isSelected();

        startSuspendedCheckbox.setEnabled(enabled);
        anyPortCheckbox.setEnabled(enabled);

        debugHostLabel.setEnabled(enabled);
        debugHostField.setEnabled(enabled);

        debugPortLabel.setEnabled(enabled && usingSpecificPort);
        debugPortField.setEnabled(enabled && usingSpecificPort);

        warningLabel.setEnabled(enabled);

        warningLabel.setVisible(enabled && usingSpecificPort);

        messageLabel.setVisible(enabled);
    }

    private String getHostToBeUsed() throws Exception {
        final String host = debugHostField.getText();
        if (StringUtils.isBlank(host)) {
            throw new Exception("Blank host");
        }
        return host;
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
            final String host = getHostToBeUsed();
            // use the port only when needed
            final int port = useAnyPort ? 0 : getPortToBeUsed();
            final String parameters = DebugParameterHelper.getRemoteDebugParameters(useAnyPort, startSuspended, host, port);
            messageLabel.setText(translator.translate("debugPanel.description.success", parameters));
        } catch (final Exception ignore) {
            final String lastValidValue = config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT);
            final String host = config.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_HOST);;
            final int port = Integer.parseInt(lastValidValue);
            final String parameters = DebugParameterHelper.getRemoteDebugParameters(useAnyPort, startSuspended, host, port);
            messageLabel.setText(translator.translate("debugPanel.description.fail", parameters));
        }
    }
}
