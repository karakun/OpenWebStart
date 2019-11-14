package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.util.concurrent.Executors;

public class DeleteRuntimeAction extends BasicAction<LocalJavaRuntime> {

    public DeleteRuntimeAction() {
        super(Translator.getInstance().translate("jvmManager.action.deleteRuntime.text"), Translator.getInstance().translate("jvmManager.action.deleteRuntime.description"));
    }

    @Override
    public void call(final LocalJavaRuntime item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                LocalRuntimeManager.getInstance().delete(item);
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(Translator.getInstance().translate("jvmManager.error.deleteFolder"), e);
            }
        });
    }
}
