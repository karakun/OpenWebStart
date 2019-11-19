package com.openwebstart.debug;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.ConfigurationConstants;

import java.util.Objects;

public enum LogWindowModes {

    CONSOLE_SHOW("loggingPanel.showLogWindowState.show", ConfigurationConstants.CONSOLE_SHOW),
    CONSOLE_HIDE("loggingPanel.showLogWindowState.hide", ConfigurationConstants.CONSOLE_HIDE),
    CONSOLE_DISABLE("loggingPanel.showLogWindowState.disable", ConfigurationConstants.CONSOLE_DISABLE);

    private final String translationKey;

    private final String propertyValue;

    LogWindowModes(final String translationKey, final String propertyValue) {
        this.translationKey = translationKey;
        this.propertyValue = propertyValue;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public String toString() {
        return Translator.getInstance().translate(translationKey);
    }

    public static LogWindowModes getForConfigValue(final String configValue) {
        if(Objects.equals(configValue, ConfigurationConstants.CONSOLE_SHOW)) {
            return CONSOLE_SHOW;
        }
        if(Objects.equals(configValue, ConfigurationConstants.CONSOLE_HIDE)) {
            return CONSOLE_HIDE;
        }
        return CONSOLE_DISABLE;
    }
}
