package com.openwebstart.logging;

import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

public class LoggingPanel extends FormPanel {

    public LoggingPanel(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "deploymentConfiguration");

        final Translator translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(config);

        int row = 0;


        final JLabel showLogWindowLabel = new JLabel(translator.translate("loggingPanel.showLogWindow.text") + ":");
        final JComboBox<LogWindowModes> showLogWindowCombobox = new JComboBox<>(new Vector<>(Arrays.asList(LogWindowModes.values())));
        showLogWindowCombobox.setToolTipText(translator.translate("loggingPanel.showLogWindow.description"));
        uiLock.update(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE, showLogWindowCombobox);
        showLogWindowCombobox.addActionListener(e -> {
            final LogWindowModes selectedItem = (LogWindowModes) showLogWindowCombobox.getSelectedItem();
            config.setProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE, selectedItem.getPropertyValue());
        });
        showLogWindowCombobox.setSelectedItem(LogWindowModes.getForConfigValue(config.getProperty(ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE)));
        addRow(row++, showLogWindowLabel, showLogWindowCombobox);


        final JCheckBox activateDebugLoggingCheckbox = new JCheckBox(translator.translate("loggingPanel.activateDebug.text"));
        activateDebugLoggingCheckbox.setToolTipText(translator.translate("loggingPanel.activateDebug.description"));
        bindToSettings(config, activateDebugLoggingCheckbox, ConfigurationConstants.KEY_ENABLE_DEBUG_LOGGING);
        addEditorRow(row++, activateDebugLoggingCheckbox);


        final JCheckBox logJnlpContentCheckbox = new JCheckBox(translator.translate("loggingPanel.logJnlpContent.text"));
        logJnlpContentCheckbox.setToolTipText(translator.translate("loggingPanel.logJnlpContent.description"));
        bindToSettings(config, logJnlpContentCheckbox, ConfigurationConstants.KEY_ENABLE_LOGGING_OF_JNLP_FILE_CONTENT);
        addEditorRow(row++, logJnlpContentCheckbox);


        final JCheckBox logToStandardOutCheckbox = new JCheckBox(translator.translate("loggingPanel.logToStandardOut.text"));
        logToStandardOutCheckbox.setToolTipText(translator.translate("loggingPanel.logToStandardOut.description"));
        bindToSettings(config, logToStandardOutCheckbox, ConfigurationConstants.KEY_ENABLE_LOGGING_TOSTREAMS);
        addEditorRow(row++, logToStandardOutCheckbox);


        final JCheckBox logInFileCheckbox = new JCheckBox(translator.translate("loggingPanel.logInFile.text"));
        logInFileCheckbox.setToolTipText(translator.translate("loggingPanel.logInFile.description"));
        bindToSettings(config, logInFileCheckbox, ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE);
        addEditorRow(row++, logInFileCheckbox);


        final JLabel logFolderLabel = new JLabel(translator.translate("loggingPanel.logFolder.text") + ":");
        final JTextField logFolderField = new JTextField();
        logFolderField.setToolTipText(translator.translate("loggingPanel.logFolder.description"));
        logFolderField.setText(PathsAndFiles.LOG_DIR.getFullPath(config));
        logFolderField.setEditable(false);
        final JButton selectFolderButton = new JButton(translator.translate("loggingPanel.selectLogFolder.text"));
        selectFolderButton.setToolTipText(translator.translate("loggingPanel.selectLogFolder.description"));
        uiLock.update(ConfigurationConstants.KEY_USER_LOG_DIR, selectFolderButton);
        selectFolderButton.addActionListener(e -> {
            final JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(logFolderField.getText()));
            chooser.setDialogTitle(translator.translate("loggingPanel.selectFolder"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                final File folder = chooser.getSelectedFile();
                if (folder.exists() && folder.isDirectory()) {
                    logFolderField.setText(folder.getAbsolutePath());
                    PathsAndFiles.LOG_DIR.setValue(folder.getAbsolutePath(), config);
                }
            }
        });
        final JPanel editorPanel = new JPanel(LayoutFactory.createBorderLayout());
        editorPanel.add(logFolderField, BorderLayout.CENTER);
        editorPanel.add(selectFolderButton, BorderLayout.EAST);
        addRow(row++, logFolderLabel, editorPanel);

        addFlexibleRow(row);
    }

    private void bindToSettings(final DeploymentConfiguration config, final JCheckBox checkbox, final String propertyName) {
        final UiLock uiLock = new UiLock(config);
        uiLock.update(propertyName, checkbox);
        checkbox.addChangeListener(e -> config.setProperty(propertyName, Boolean.valueOf(checkbox.isSelected()).toString()));
        checkbox.setSelected(Boolean.parseBoolean(config.getProperty(propertyName)));
    }
}
