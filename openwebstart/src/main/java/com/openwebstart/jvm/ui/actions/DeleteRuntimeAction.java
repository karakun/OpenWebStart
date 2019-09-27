package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;

public class DeleteRuntimeAction extends BasicAction<LocalJavaRuntime> {

    public DeleteRuntimeAction() {
        super("Delete JVM", "Delete the JVM");
    }

    @Override
    public void call(final LocalJavaRuntime item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                LocalRuntimeManager.getInstance().delete(item);
            } catch (final Exception e) {
                DialogFactory.showErrorDialog("Can not delete local folder", e);
            }
        });
    }
}
