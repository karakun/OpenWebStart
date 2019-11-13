package com.openwebstart.app.ui.actions;

import com.openwebstart.app.Application;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.os.MenuAndDesktopEntryHandler;
import com.openwebstart.ui.BasicAction;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.JNLPFile;

public class CreateShortcutAction extends BasicAction<Application> {

    public CreateShortcutAction() {
        super(Translator.getInstance().translate("appManager.action.createShortcut.text"), Translator.getInstance().translate("appManager.action.createShortcut.description"));
    }

    @Override
    public void call(final Application item) {
        Assert.requireNonNull(item, "item");
        try {
            final MenuAndDesktopEntryHandler handler = new MenuAndDesktopEntryHandler();
            handler.askForIntegration(item.getJnlpFile());
        } catch (final Exception e) {
            //TODO: Translation
            DialogFactory.showErrorDialog("Can not create shortcut", e);
        }
    }
}

