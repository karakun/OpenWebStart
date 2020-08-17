package com.openwebstart.ui.dialogs;

import com.openwebstart.ui.ModalDialog;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.concurrent.CompletableFuture;

public abstract class DialogWithResult<R> extends ModalDialog {

    private R result;

    public DialogWithResult(final String title) {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(title);
    }

    protected abstract JPanel createContentPane();

    protected void closeWithResult(final R result) {
        this.result = result;
        this.close();
    }

    public R showAndWaitForResult() {
        if (SwingUtilities.isEventDispatchThread()) {
            getContentPane().removeAll();
            getContentPane().add(createContentPane());
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            return result;
        } else {
            final CompletableFuture<R> result = new CompletableFuture<>();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    pack();
                    final R r = showAndWaitForResult();
                    result.complete(r);
                });
                return result.get();
            } catch (Exception e) {
                throw new RuntimeException("Error in handling dialog!", e);
            }
        }
    }
}
