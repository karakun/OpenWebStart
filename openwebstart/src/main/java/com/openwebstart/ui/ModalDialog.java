package com.openwebstart.ui;

import com.openwebstart.util.LayoutFactory;

import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;

public class ModalDialog extends JDialog {

    private final JPanel dialogPanel;

    private Container currentContentPane;
    public ModalDialog() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);

        try {
            setIconImages(AppIcon.getAllIcons());
        } catch (Exception e) {
            //ignore
        }
        dialogPanel = new JPanel(LayoutFactory.createBorderLayout());
        dialogPanel.add(new OpenWebStartDialogHeader(), BorderLayout.NORTH);
        super.setContentPane(dialogPanel);
    }

    @Override
    public void setContentPane(final Container contentPane) {
        if (currentContentPane != null) {
            dialogPanel.remove(currentContentPane);
        }
        dialogPanel.add(contentPane, BorderLayout.CENTER);
        currentContentPane = contentPane;
    }

    public void showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        dispose();
    }
}
