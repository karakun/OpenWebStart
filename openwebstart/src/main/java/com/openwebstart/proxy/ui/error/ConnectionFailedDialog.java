package com.openwebstart.proxy.ui.error;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

public class ConnectionFailedDialog extends ProxyErrorDialog {

    private final static String TITLE_KEY = "xxx.xxx.xxx";
    private final static String MESSAGE_KEY = "xxx.xxx.xxx";

    public ConnectionFailedDialog() {
        super(Translator.getInstance().translate(TITLE_KEY),
                Translator.getInstance().translate(MESSAGE_KEY));
    }
}
