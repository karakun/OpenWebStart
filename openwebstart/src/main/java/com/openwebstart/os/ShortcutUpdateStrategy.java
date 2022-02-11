package com.openwebstart.os;

import com.openwebstart.ui.Translatable;

import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_SHORTCUT_UPDATE_STRATEGY;

public enum ShortcutUpdateStrategy implements Translatable {

    /**
     * Overwrite the existing shortcuts.
     */
    OVERWRITE("desktop.integration.shortcutUpdateStrategy.overwrite"),

    /**
     * Keep the existing shortcuts as they are.
     */
    PRESERVE("desktop.integration.shortcutUpdateStrategy.preserve"),

    /**
     * Create the shortcut with unique name
     */
    UNIQUE_NAME("desktop.integration.shortcutUpdateStrategy.uniqueName");

    private final String translationKey;

    ShortcutUpdateStrategy(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public static ShortcutUpdateStrategy get(final String value) {
        for (final ShortcutUpdateStrategy strategy : values()) {
            if (strategy.name().equals(value)) {
                return strategy;
            }
        }
        return DEFAULT_SHORTCUT_UPDATE_STRATEGY;
    }
}
