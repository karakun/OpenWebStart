package com.openwebstart.update;

import com.install4j.api.update.UpdateSchedule;
import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.util.Optional;

public interface UpdatePanelConfigConstants {

    String CHECK_FOR_UPDATED_PARAM_NAME = "ows.update.activated";

    String CHECK_FOR_UPDATED_NOW_PARAM_NAME = "ows.checkUpdate";

    String UPDATED_STRATEGY_SETTINGS_PARAM_NAME = "ows.update.strategy.settings";

    String UPDATED_STRATEGY_LAUNCH_PARAM_NAME = "ows.update.strategy.launch";

    static UpdateSchedule getUpdateScheduleForSettings(final DeploymentConfiguration configuration) {
        Assert.requireNonNull(configuration, "configuration");

        return Optional.ofNullable(configuration.getProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME))
                .map(v -> UpdateSchedule.valueOf(v))
                .orElse(UpdateSchedule.ON_EVERY_START);
    }

    static UpdateSchedule getUpdateScheduleForLauncher(final DeploymentConfiguration configuration) {
        Assert.requireNonNull(configuration, "configuration");

        return Optional.ofNullable(configuration.getProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME))
                .map(v -> UpdateSchedule.valueOf(v))
                .orElse(UpdateSchedule.WEEKLY);
    }

    static boolean isAutoUpdateActivated(final DeploymentConfiguration configuration) {
        Assert.requireNonNull(configuration, "configuration");

        return Optional.ofNullable(configuration.getProperty(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME))
                .map(v -> Boolean.parseBoolean(v))
                .orElse(true);
    }
}
