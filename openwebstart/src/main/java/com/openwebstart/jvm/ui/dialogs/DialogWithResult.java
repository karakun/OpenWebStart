package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.ui.Images;
import com.openwebstart.ui.IconComponent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class DialogWithResult<R> extends JDialog {

    private R result;

    public DialogWithResult(final String title, final String message, final DialogButton<R>... buttons) {
        this(title, message, new ImageIcon(Images.QUESTION_64_URL), buttons);
    }

    public DialogWithResult(final String title, final String message, final ImageIcon imageIcon, final DialogButton<R>... buttons) {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(title);

        final IconComponent icon = new IconComponent(imageIcon);

        final JTextArea messageLabel = new JTextArea(message);
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        messageLabel.setColumns(50);

        final JPanel messageWrapperPanel = new JPanel();
        messageWrapperPanel.setLayout(new BorderLayout(12, 12));
        messageWrapperPanel.add(icon, BorderLayout.WEST);
        messageWrapperPanel.add(messageLabel, BorderLayout.CENTER);

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionWrapperPanel.add(Box.createHorizontalGlue());

        Arrays.asList(buttons).forEach(b -> {
            final JButton button = new JButton(b.getText());
            if (b.getDescription() != null) {
                button.setToolTipText(b.getDescription());
            }
            button.addActionListener(e -> {
                final R result = b.getOnAction().get();
                close(result);
            });
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.add(messageWrapperPanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        add(contentPanel);
    }

    private void close(final R result) {
        this.result = result;
        this.setVisible(false);
        this.dispose();
    }

    public R showAndWait() {
        if (SwingUtilities.isEventDispatchThread()) {
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

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final String msg1 = "This is a long text that should be displayed in more than 1 line. This is a long text that should be displayed in more than 1 line. This is a long text that should be displayed in more than 1 line.";
        final String msg2 = "Connection failed for URL: https://docs.oracle.com/javase/tutorialJWS/samples/uiswing/AccessibleScrollDemoProject/AccessibleScrollDemo.jnlp." +
                "\n\nDo you want to continue with no proxy or exit the application?";
        final DialogButton<Integer> exitButton = new DialogButton<>("Exit", () -> 0);

        new DialogWithResult<>("Title", msg1, exitButton).showAndWait();
        new DialogWithResult<>("Title", msg2, exitButton).showAndWait();
    }
}
