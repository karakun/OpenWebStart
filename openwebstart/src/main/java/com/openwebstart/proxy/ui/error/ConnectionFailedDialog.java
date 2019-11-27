package com.openwebstart.proxy.ui.error;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

public class ConnectionFailedDialog extends ProxyErrorDialog {

    private final static String TITLE_KEY = "proxy.error.connectionFailed.title";
    private final static String MESSAGE_KEY = "proxy.error.connectionFailed.message";

    public ConnectionFailedDialog() {
        super(Translator.getInstance().translate(TITLE_KEY),
                Translator.getInstance().translate(MESSAGE_KEY));
    }
}
