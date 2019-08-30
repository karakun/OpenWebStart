package com.openwebstart.jvm;

import com.openwebstart.jvm.ui.util.Translatable;

public enum RuntimeUpdateStrategy implements Translatable {

    DO_NOTHING_ON_LOCAL_MATCH("Use local if available"),
    ASK_FOR_UPDATE_ON_LOCAL_MATCH("Ask if newer version should be downloaded"),
    AUTOMATICALLY_DOWNLOAD("Always download newer version");

    private final String translationKey;

    RuntimeUpdateStrategy(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
