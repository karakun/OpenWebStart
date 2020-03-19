package com.openwebstart.ui;

import javax.swing.JDialog;

public class ModalDialog extends JDialog {

    public ModalDialog() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    public void showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        dispose();
    }
}
