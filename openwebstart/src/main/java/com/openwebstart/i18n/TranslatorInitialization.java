package com.openwebstart.i18n;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.util.ResourceBundle;

public class TranslatorInitialization {

    public static void init() {
        Translator.addBundle("i18n");
    }
}
