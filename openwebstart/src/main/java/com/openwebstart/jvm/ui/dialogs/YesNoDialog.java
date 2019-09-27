package com.openwebstart.jvm.ui.dialogs;

public class YesNoDialog extends DialogWithResult<Boolean> {

    public YesNoDialog(final String title, final String message) {
        super(title, message, new DialogButton<>("Yes", () -> true), new DialogButton<>("No", () -> false));
    }
}
