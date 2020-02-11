package com.openwebstart.proxy.ui.error;

import com.openwebstart.proxy.ProxyProviderType;
import com.openwebstart.ui.dialogs.DialogButton;
import com.openwebstart.ui.dialogs.SimpleDialogWithResult;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

public class UnsupportedFeatureDialog extends SimpleDialogWithResult<ProxyDialogResult> {

    private final static String DIALOG_TITLE = "proxy.unsupportedFeatureDialog.title";
    private final static String DIALOG_MESSAGE = "proxy.unsupportedFeatureDialog.message";
    private final static String MANUAL_PROXY_KEY = "proxy.type.manual.name";
    private final static String PAC_PROXY_KEY = "proxy.type.pac.name";
    private final static String FIREFOX_PROXY_KEY = "proxy.type.firefox.name";
    private final static String SYSTEM_PROXY_KEY = "proxy.type.system.name";
    private final static String UNKNOWN_PROXY_KEY = "proxy.type.unknown.name";
    private final static String EXIT_TITLE_KEY = "proxy.unsupportedFeatureDialog.exitAction.title";
    private final static String EXIT_DESCRIPTION_KEY = "proxy.unsupportedFeatureDialog.exitAction.description";
    private final static String CONTINUE_TITLE_KEY = "proxy.unsupportedFeatureDialog.continue.title";
    private final static String CONTINUE_DESCRIPTION_KEY = "proxy.unsupportedFeatureDialog.continue.description";

    public UnsupportedFeatureDialog(final ProxyProviderType proxyType, final String featureName) {
        super(getTranslatedTitle(), getTranslatedMessage(proxyType, featureName), createContinueButton(), createExitButtons());
    }

    private static String getTranslatedTitle() {
        return Translator.getInstance().translate(DIALOG_TITLE);
    }

    private static String getTranslatedProxyName(final ProxyProviderType proxyType) {
        if(proxyType == ProxyProviderType.MANUAL_HOSTS) {
            return Translator.getInstance().translate(MANUAL_PROXY_KEY);
        } else if(proxyType == ProxyProviderType.MANUAL_PAC_URL) {
            return Translator.getInstance().translate(PAC_PROXY_KEY);
        } else if(proxyType == ProxyProviderType.FIREFOX) {
            return Translator.getInstance().translate(FIREFOX_PROXY_KEY);
        } else if(proxyType == ProxyProviderType.OPERATION_SYSTEM) {
            return Translator.getInstance().translate(SYSTEM_PROXY_KEY);
        } else {
            return Translator.getInstance().translate(UNKNOWN_PROXY_KEY);
        }
    }

    private static String getTranslatedMessage(final ProxyProviderType proxyType, final String featureName) {
        final String proxyName = getTranslatedProxyName(proxyType);
        return Translator.getInstance().translate(DIALOG_MESSAGE, proxyName, featureName);
    }

    protected static DialogButton<ProxyDialogResult> createExitButtons() {
        final String exitText = Translator.getInstance().translate(EXIT_TITLE_KEY);
        final String exitDescription = Translator.getInstance().translate(EXIT_DESCRIPTION_KEY);

        return new DialogButton<>(exitText, () -> ProxyDialogResult.EXIT, exitDescription);
    }

    protected static DialogButton<ProxyDialogResult> createContinueButton() {
        final String continueText = Translator.getInstance().translate(CONTINUE_TITLE_KEY);
        final String continueDescription = Translator.getInstance().translate(CONTINUE_DESCRIPTION_KEY);

        return new DialogButton<>(continueText, () -> ProxyDialogResult.CONTINUE, continueDescription);
    }

    public static void main(String[] args){
        Translator.addBundle("i18n");
        final String featureName = Translator.getInstance().translate("proxy.unsupportedFeature.httpUser");
        final ProxyDialogResult result = new UnsupportedFeatureDialog(ProxyProviderType.OPERATION_SYSTEM, featureName).showAndWait();
    }
}

