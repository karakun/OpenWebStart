package com.openwebstart.os;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.ui.Translatable;

public enum ShortcutUpdateStrategy implements Translatable {
    OVERWRITE("desktop.integration.shortcutUpdateStrategy.overwrite"),
    PRESERVE("desktop.integration.shortcutUpdateStrategy.preserve"),
    UNIQUE_NAME("desktop.integration.shortcutUpdateStrategy.uniqueName"),

    ;

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
        return OwsDefaultsProvider.DEFAULT_SHORTCUT_UPDATE_STRATEGY;
    }
}
