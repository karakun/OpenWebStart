package com.openwebstart.jvm;

import com.openwebstart.ui.Translatable;

public enum RuntimeUpdateStrategy implements Translatable {

    /**
     * If a local runtime is found, it will be used.
     * If no local runtime is found, an error will be shown.
     */
    NO_REMOTE("jvmManager.updateStrategy.noRemote"),

    /**
     * If a local runtime is found, the remote endpoint is not checked for new versions.
     * If no local runtime could be found, the remote endpoint is checked for a version.
     */
    DO_NOTHING_ON_LOCAL_MATCH("jvmManager.updateStrategy.doNothing"),

    /**
     * If a local runtime is found, the remote endpoint is checked for newer versions.
     * If a newer version is found, the user is prompted whether to update or not.
     * If no local runtime could be found, the remote endpoint is checked for a version.
     */
    ASK_FOR_UPDATE_ON_LOCAL_MATCH("jvmManager.updateStrategy.askForUpdate"),

    /**
     * Always the newest version, always check and update from remote endpoint.
     */
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
