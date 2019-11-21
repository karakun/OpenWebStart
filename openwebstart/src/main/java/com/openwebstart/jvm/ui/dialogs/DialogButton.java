package com.openwebstart.jvm.ui.dialogs;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.function.Supplier;

public class DialogButton<R> {

    private final String text;

    private final Supplier<R> onAction;

    public DialogButton(final String text, final Supplier<R> onAction) {
        this.text = Assert.requireNonBlank(text, "text");
        this.onAction = Assert.requireNonNull(onAction, "onAction");
    }

    public String getText() {
        return text;
    }

    public Supplier<R> getOnAction() {
        return onAction;
    }
}
