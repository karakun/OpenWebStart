package com.openwebstart.jvm.ui.dialogs;

import javax.swing.JDialog;

public class ModalDialog extends JDialog {

    public ModalDialog() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    public void showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected void close() {
        setVisible(false);
        dispose();
    }
}
