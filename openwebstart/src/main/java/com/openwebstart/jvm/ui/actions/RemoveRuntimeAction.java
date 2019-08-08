package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;

public class RemoveRuntimeAction extends BasicAction<LocalJavaRuntime> {

    public RemoveRuntimeAction() {
        super("remove JVM", "remove the JVM from the list");
    }

    @Override
    public void call(final LocalJavaRuntime item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                LocalRuntimeManager.getInstance().remove(item);
            } catch (final Exception e) {
                SwingUtilities.invokeLater(() -> new ErrorDialog("Can not remove local folder", e).showAndWait());
            }
        });
    }
}
