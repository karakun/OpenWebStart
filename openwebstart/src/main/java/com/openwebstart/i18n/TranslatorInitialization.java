package com.openwebstart.i18n;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.util.ResourceBundle;

public class TranslatorInitialization {

    public static void init() {
        final Translator translator = Translator.getInstance();
        final ResourceBundle icedTeaWebBundle = translator.getResources();
        final ResourceBundle resourceBundle = OpenWebStartResourceBundle.getBundle(icedTeaWebBundle, "i18n");
        translator.changeResourceBundle(resourceBundle);
    }
}
