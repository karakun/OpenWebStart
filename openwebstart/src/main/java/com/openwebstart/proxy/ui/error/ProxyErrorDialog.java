package com.openwebstart.proxy.ui.error;

import com.openwebstart.jvm.ui.dialogs.DialogButton;
import com.openwebstart.jvm.ui.dialogs.DialogWithResult;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

public abstract class ProxyErrorDialog extends DialogWithResult<ProxyDialogResult> {

    private final static String EXIT_KEY = "xxx.xxx.xxx";
    private final static String CONTINUE_KEY = "xxx.xxx.xxx";

    public ProxyErrorDialog(final String title, final String message) {
        super(title, message, createButtons());
    }

    protected static DialogButton<ProxyDialogResult>[] createButtons() {
        final String exitText = Translator.getInstance().translate(EXIT_KEY);
        final String continueText = Translator.getInstance().translate(CONTINUE_KEY);
        final DialogButton<ProxyDialogResult> exitButton = new DialogButton<>(exitText, () -> ProxyDialogResult.EXIT);
        final DialogButton<ProxyDialogResult> continueButton = new DialogButton<>(continueText, () -> ProxyDialogResult.CONTINUE_WITH_NO_PROXY);
        return new DialogButton[]{exitButton, continueButton};
    }
}

