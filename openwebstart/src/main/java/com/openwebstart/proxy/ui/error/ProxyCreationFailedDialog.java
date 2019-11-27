package com.openwebstart.proxy.ui.error;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

public class ProxyCreationFailedDialog extends ProxyErrorDialog {

    private final static String TITLE_KEY = "proxy.error.creationFailed.title";
    private final static String MESSAGE_KEY = "proxy.error.creationFailed.message";

    public ProxyCreationFailedDialog() {
        super(Translator.getInstance().translate(TITLE_KEY),
                Translator.getInstance().translate(MESSAGE_KEY));
    }
}
