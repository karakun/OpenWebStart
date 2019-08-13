package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.ui.IconComponent;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialog extends JDialog {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDialog.class);

    public ErrorDialog(final String message, final Exception error) {
        Assert.requireNonNull(error, "error");
        LOG.error("Runtime Error", error);

        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("Error");

        final ImageIcon imageIcon = new ImageIcon(IconComponent.class.getResource("error-64.png"));
        final IconComponent downloadIcon = new IconComponent(imageIcon);

        final JLabel messageLabel = new JLabel(message);

        final JTextArea exceptionDetailsArea = new JTextArea();
        exceptionDetailsArea.setEditable(false);
        StringWriter writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));
        exceptionDetailsArea.setText(writer.getBuffer().toString());

        final JButton okButton = new JButton("close");
        okButton.addActionListener(e -> dispose());

        final JPanel messageWrapperPanel = new JPanel();
        messageWrapperPanel.setLayout(new BorderLayout(12, 12));
        messageWrapperPanel.add(downloadIcon, BorderLayout.WEST);
        messageWrapperPanel.add(messageLabel, BorderLayout.CENTER);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(messageWrapperPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(exceptionDetailsArea), BorderLayout.CENTER);

        final JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BorderLayout());
        actionPanel.add(okButton, BorderLayout.EAST);
        panel.add(actionPanel, BorderLayout.SOUTH);
        add(panel);
    }

    public void showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
