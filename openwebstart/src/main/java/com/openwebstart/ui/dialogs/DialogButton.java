package com.openwebstart.ui.dialogs;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.function.Supplier;

public class DialogButton<R> {

    private final String text;

    private final Supplier<R> onAction;

    private final String description;

    public DialogButton(final String text, final Supplier<R> onAction) {
        this(text, onAction, null);
    }

    public DialogButton(final String text, final Supplier<R> onAction, final String description) {
        this.text = Assert.requireNonBlank(text, "text");
        this.onAction = Assert.requireNonNull(onAction, "onAction");
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return text;
    }

    public Supplier<R> getOnAction() {
        return onAction;
    }
}
