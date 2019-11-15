package com.openwebstart.jvm;

import com.openwebstart.ui.Translatable;

public enum RuntimeUpdateStrategy implements Translatable {

    DO_NOTHING_ON_LOCAL_MATCH("jvmManager.updateStrategy.doNothing"),
    ASK_FOR_UPDATE_ON_LOCAL_MATCH("jvmManager.updateStrategy.askForUpdate"),
    AUTOMATICALLY_DOWNLOAD("jvmManager.updateStrategy.download");

    private final String translationKey;

    RuntimeUpdateStrategy(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
