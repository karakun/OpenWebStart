package com.openwebstart.os;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.ui.Translatable;

public enum ShortcutUpdateStrategy implements Translatable {

    /**
     * Overwrite the existing shortcuts.
     */
    OVERWRITE ("desktop.integration.updateStrategy.overwrite"),

    /**
     * Keep the existing shortcuts as they are.
     */
    KEEP ("desktop.integration.updateStrategy.keep"),

    /**
     * Create the shortcut with unique name
     */
    UNIQUE_NAME ("desktop.integration.updateStrategy.unique");

    private final String translationKey;

    ShortcutUpdateStrategy(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public static ShortcutUpdateStrategy get(final String propertyValue) {
        for(ShortcutUpdateStrategy strategy : ShortcutUpdateStrategy.values()) {
            if (strategy.name().equals(propertyValue)) {
                return strategy;
            }
        }
        return OwsDefaultsProvider.SHORTCUT_UPDATE_STRATEGY_DEFAULT_VALUE;
    }
}
