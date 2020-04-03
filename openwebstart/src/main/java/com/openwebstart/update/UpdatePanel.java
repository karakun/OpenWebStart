package com.openwebstart.update;

import com.install4j.api.update.UpdateDescriptorEntry;
import com.install4j.api.update.UpdateSchedule;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.install4j.Install4JUpdateHandler;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;

public class UpdatePanel extends FormPanel {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePanel.class);

    private final JCheckBox checkForUpdatesCheckbox;

    private final JComboBox<UpdateSchedule> updateStrategyForSettingsComboBox;

    private final JComboBox<UpdateSchedule> updateStrategyForLaunchComboBox;

    private final DeploymentConfiguration configuration;

    private final Translator translator;

    public UpdatePanel(final DeploymentConfiguration configuration) {
        this.configuration = Assert.requireNonNull(configuration, "configuration");
        this.translator = Translator.getInstance();
        final UiLock uiLock = new UiLock(configuration);

        this.checkForUpdatesCheckbox = new JCheckBox();
        this.checkForUpdatesCheckbox.setText(translator.translate("updatesPanel.checkForUpdates.text"));
        this.checkForUpdatesCheckbox.setToolTipText(translator.translate("updatesPanel.checkForUpdates.description"));
        this.checkForUpdatesCheckbox.setSelected(UpdatePanelConfigConstants.isAutoUpdateActivated(configuration));
        uiLock.update(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME, checkForUpdatesCheckbox);
        this.checkForUpdatesCheckbox.addChangeListener(e -> save());

        this.updateStrategyForSettingsComboBox = new JComboBox<>();
        this.updateStrategyForSettingsComboBox.setToolTipText(translator.translate("updatesPanel.updateStrategyForSettings.description"));
        this.updateStrategyForSettingsComboBox.setModel(new DefaultComboBoxModel<>(UpdateSchedule.values()));
        this.updateStrategyForSettingsComboBox.setSelectedItem(UpdatePanelConfigConstants.getUpdateScheduleForSettings(configuration));
        this.updateStrategyForSettingsComboBox.setRenderer(new UpdateScheduleRenderer());
        uiLock.update(UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME, updateStrategyForSettingsComboBox);
        this.updateStrategyForSettingsComboBox.addItemListener(e -> save());
        final JLabel updateStrategyForSettingsLabel = new JLabel(translator.translate("updatesPanel.updateStrategyForSettings.text"));


        this.updateStrategyForLaunchComboBox = new JComboBox<>();
        this.updateStrategyForSettingsComboBox.setToolTipText(translator.translate("updatesPanel.updateStrategyForLaunch.description"));
        this.updateStrategyForLaunchComboBox.setModel(new DefaultComboBoxModel<>(UpdateSchedule.values()));
        this.updateStrategyForLaunchComboBox.setSelectedItem(UpdatePanelConfigConstants.getUpdateScheduleForLauncher(configuration));
        this.updateStrategyForLaunchComboBox.setRenderer(new UpdateScheduleRenderer());
        uiLock.update(UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME, updateStrategyForLaunchComboBox);
        this.updateStrategyForLaunchComboBox.addItemListener(e -> save());
        final JLabel updateStrategyForLaunchLabel = new JLabel(translator.translate("updatesPanel.updateStrategyForLaunch.text"));

        final JButton checkForUpdateButton = new JButton();
        checkForUpdateButton.setText(translator.translate("updatesPanel.checkNowForUpdates.text"));
        checkForUpdateButton.setToolTipText(translator.translate("updatesPanel.checkNowForUpdates.description"));
        uiLock.update(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_NOW_PARAM_NAME, checkForUpdateButton);
        checkForUpdateButton.addActionListener(e -> {
            checkForUpdateButton.setEnabled(false);
            getNonDaemonExecutorService().execute(() -> {
                try {
                    if (Install4JUpdateHandler.hasUpdate()) {
                        Install4JUpdateHandler.doUpdate();
                    } else {
                        DialogFactory.showConfirmDialog(translator.translate("updatesPanel.dialog.noUpdate.title"), translator.translate("updatesPanel.dialog.noUpdate.message"));
                    }
                } catch (final Exception ex) {
                    DialogFactory.showErrorDialog(translator.translate("updatesPanel.error.updateCheck"), ex);
                } finally {
                    SwingUtils.invokeLater(() -> checkForUpdateButton.setEnabled(true));
                }
            });
        });
        if (checkForUpdateButton.isEnabled()) {
            getNonDaemonExecutorService().execute(() -> {
                try {
                    Install4JUpdateHandler.getUpdate()
                            .map(UpdateDescriptorEntry::getNewVersion)
                            .map(v -> translator.translate("updatesPanel.installConcreteVersion", v))
                            .ifPresent(s -> SwingUtils.invokeLater(() -> checkForUpdateButton.setText(s)));
                } catch (final Exception ex) {
                    LOG.error("Error while trying to find an OpenWebStart update", ex);
                }
            });
        }

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addEditorRow(0, checkForUpdatesCheckbox);
        addRow(1, updateStrategyForSettingsLabel, updateStrategyForSettingsComboBox);
        addRow(2, updateStrategyForLaunchLabel, updateStrategyForLaunchComboBox);
        addEditorRow(3, checkForUpdateButton);
        addFlexibleRow(4);
    }

    private void save() {
        configuration.setProperty(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME, Boolean.valueOf(this.checkForUpdatesCheckbox.isSelected()).toString());
        configuration.setProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME, ((UpdateSchedule) updateStrategyForSettingsComboBox.getSelectedItem()).name());
        configuration.setProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME, ((UpdateSchedule) updateStrategyForLaunchComboBox.getSelectedItem()).name());
        try {
            configuration.save();
        } catch (Exception e) {
            DialogFactory.showErrorDialog(translator.translate("error.saveConfig"), e);
        }
    }
}
