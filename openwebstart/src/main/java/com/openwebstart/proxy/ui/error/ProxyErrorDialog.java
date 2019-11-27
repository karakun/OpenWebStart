package com.openwebstart.proxy.ui.error;

import com.openwebstart.jvm.ui.dialogs.DialogButton;
import com.openwebstart.jvm.ui.dialogs.DialogWithResult;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

public abstract class ProxyErrorDialog extends DialogWithResult<ProxyDialogResult> {

    private final static String EXIT_TITLE_KEY = "proxy.error.exitAction.title";
    private final static String EXIT_DESCRIPTION_KEY = "proxy.error.exitAction.description";
    private final static String CONTINUE_WITH_NO_PROXY_TITLE_KEY = "proxy.error.continueWithNoProxyAction.title";
    private final static String CONTINUE_WITH_NO_PROXY_DESCRIPTION_KEY = "proxy.error.continueWithNoProxyAction.description";

    public ProxyErrorDialog(final String title, final String message) {
        super(title, message, createNoProxyButton(), createExitButtons());
    }

    protected static DialogButton<ProxyDialogResult> createExitButtons() {
        final String exitText = Translator.getInstance().translate(EXIT_TITLE_KEY);
        final String exitDescription = Translator.getInstance().translate(EXIT_DESCRIPTION_KEY);

        return new DialogButton<>(exitText, () -> ProxyDialogResult.EXIT, exitDescription);
    }

    protected static DialogButton<ProxyDialogResult> createNoProxyButton() {
        final String continueText = Translator.getInstance().translate(CONTINUE_WITH_NO_PROXY_TITLE_KEY);
        final String continueDescription = Translator.getInstance().translate(CONTINUE_WITH_NO_PROXY_DESCRIPTION_KEY);

        return new DialogButton<>(continueText, () -> ProxyDialogResult.CONTINUE_WITH_NO_PROXY, continueDescription);
    }
}

