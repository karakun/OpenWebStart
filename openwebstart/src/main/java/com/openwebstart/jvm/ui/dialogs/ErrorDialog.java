package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.ui.Images;
import com.openwebstart.jvm.ui.util.IconComponent;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialog extends ModalDialog {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDialog.class);

    public ErrorDialog(final String message, final Exception error) {
        Assert.requireNonNull(error, "error");
        LOG.error("Error: " + message, error);

        final Translator translator = Translator.getInstance();

        setTitle(translator.translate("dialog.error.title"));

        final ImageIcon imageIcon = new ImageIcon(Images.ERROR_64_URL);
        final IconComponent downloadIcon = new IconComponent(imageIcon);

        final JLabel messageLabel = new JLabel(message);

        final JTextArea exceptionDetailsArea = new JTextArea();
        exceptionDetailsArea.setEditable(false);
        StringWriter writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));
        exceptionDetailsArea.setText(writer.getBuffer().toString());

        final JButton closeButton = new JButton(translator.translate("action.close"));
        closeButton.addActionListener(e -> close());

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
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.LINE_AXIS));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionPanel.add(Box.createHorizontalGlue());
        actionPanel.add(closeButton);

        panel.add(actionPanel, BorderLayout.SOUTH);
        add(panel);
    }
}
