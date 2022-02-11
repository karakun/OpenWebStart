package com.openwebstart.os;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.ui.Translatable;

public enum ShortcutUpdateStrategy implements Translatable {

    /**
     * Overwrite the existing shortcuts.
     */
    OVERWRITE ("desktop.integration.shortcutUpdateStrategy.overwrite"),

    /**
     * Keep the existing shortcuts as they are.
     */
    KEEP ("desktop.integration.shortcutUpdateStrategy.keep"),

    /**
     * Create the shortcut with unique name
     */
    UNIQUE_NAME ("desktop.integration.shortcutUpdateStrategy.uniqueName");

    private final String translationKey;

    ShortcutUpdateStrategy(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public static ShortcutUpdateStrategy get(final String propertyValue) {
        for(final ShortcutUpdateStrategy strategy : ShortcutUpdateStrategy.values()) {
            if (strategy.name().equals(propertyValue)) {
                return strategy;
            }
        }
        return OwsDefaultsProvider.DEFAULT_SHORTCUT_UPDATE_STRATEGY;
    }
}
