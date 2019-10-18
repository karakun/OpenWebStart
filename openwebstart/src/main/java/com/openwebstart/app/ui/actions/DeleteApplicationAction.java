package com.openwebstart.app.ui.actions;

import com.openwebstart.app.Application;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.ui.BasicAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

public class DeleteApplicationAction extends BasicAction<Application> {

    public DeleteApplicationAction() {
        super(Translator.getInstance().translate("appManager.action.deleteApplication.text"), Translator.getInstance().translate("appManager.action.deleteApplication.description"));
    }

    @Override
    public void call(final Application item) {
        DialogFactory.showErrorDialog("Action not implemented!", new RuntimeException("Not yet implemented"));
    }
}
