package com.openwebstart.jvm.ui.dialogs;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

public class YesNoDialog extends DialogWithResult<Boolean> {

    public YesNoDialog(final String title, final String message) {
        super(title, message, createYesButton(), createNoButton());
    }

    private static DialogButton<Boolean> createYesButton() {
        final Translator translator = Translator.getInstance();
        return new DialogButton<>(translator.translate("action.yes"), () -> true);
    }

    private static DialogButton<Boolean> createNoButton() {
        final Translator translator = Translator.getInstance();
        return new DialogButton<>(translator.translate("action.no"), () -> false);
    }
}
