package com.openwebstart.ui;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.jvm.ui.Images;
import com.openwebstart.jvm.ui.util.IconComponent;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;

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
        exceptionDetailsArea.setRows(10);
        exceptionDetailsArea.setColumns(32);
        exceptionDetailsArea.setText(getStackTrace(error));
        final JScrollPane scrollPane = new JScrollPane(exceptionDetailsArea);

        final JButton closeButton = new JButton(translator.translate("action.close"));
        closeButton.addActionListener(e -> close());

        final JPanel messageWrapperPanel = new JPanel();
        messageWrapperPanel.setLayout(new BorderLayout(12, 12));
        messageWrapperPanel.add(downloadIcon, BorderLayout.WEST);
        messageWrapperPanel.add(messageLabel, BorderLayout.CENTER);

        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setLayout(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        mainPanel.add(messageWrapperPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        scrollPane.setVisible(false);
        final JButton showDetails = new JButton(translator.translate("action.showDetails"));
        showDetails.addActionListener(e -> {
            if(!scrollPane.isVisible()) {
                scrollPane.setVisible(true);
                showDetails.setText(translator.translate("action.hideDetails"));
            } else {
                scrollPane.setVisible(false);
                showDetails.setText(translator.translate("action.showDetails"));
            }
            this.pack();
        });


        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(ButtonPanelFactory.createButtonPanel(showDetails, closeButton), BorderLayout.SOUTH);

        add(contentPane);
    }

    private String getStackTrace(final Exception exception) {
        Assert.requireNonNull(exception, "exception");
        final StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }

    public static void show(final String message, final Exception error) {
        final Runnable dialogHandler = () -> new ErrorDialog(message, error).showAndWait();

        if(SwingUtils.isEventDispatchThread()) {
            dialogHandler.run();
        } else {
            try {
                final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
                SwingUtilities.invokeAndWait(() -> {
                    dialogHandler.run();
                    completableFuture.complete(null);
                });
                completableFuture.get();
            } catch (final Exception e) {
                throw new RuntimeException("Internal runtime error while handling dialog", e);
            }
        }
    }

}
