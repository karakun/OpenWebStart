package com.openwebstart.ui.dialogs;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.concurrent.CompletableFuture;

public abstract class DialogWithResult<R> extends JDialog {

    private R result;

    public DialogWithResult(final String title) {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(title);
    }

    protected abstract JPanel createContentPane();

    protected void close(final R result) {
        this.result = result;
        this.setVisible(false);
        this.dispose();
    }

    public R showAndWait() {
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
                    final R r = showAndWait();
                    result.complete(r);
                });
                return result.get();
            } catch (Exception e) {
                throw new RuntimeException("Error in handling dialog!", e);
            }
        }
    }
}
