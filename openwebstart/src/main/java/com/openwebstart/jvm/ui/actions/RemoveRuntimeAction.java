package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;

public class RemoveRuntimeAction extends BasicAction<LocalJavaRuntime> {

    public RemoveRuntimeAction() {
        super(Translator.getInstance().translate("jvmManager.action.removeJvm.text"), Translator.getInstance().translate("jvmManager.action.removeJvm.description"));
    }

    @Override
    public void call(final LocalJavaRuntime item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                LocalRuntimeManager.getInstance().remove(item);
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(Translator.getInstance().translate("jvmManager.error.deleteFolder"), e);
            }
        });
    }
}
