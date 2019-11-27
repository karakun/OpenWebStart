package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.ui.Images;
import com.openwebstart.ui.IconComponent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(title);

        final IconComponent icon = new IconComponent(imageIcon);

        final JLabel messageLabel = new JLabel(message);

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
            button.addActionListener(e -> {
                final R result = b.getOnAction().get();
                close(result);
            });
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.add(messageWrapperPanel, BorderLayout.NORTH);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        add(contentPanel);

    }

    private void close(final R result) {
        this.result = result;
        this.setVisible(false);
        this.dispose();
    }

    public R showAndWait() {
        if(SwingUtilities.isEventDispatchThread()) {
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            return result;
        } else {
            final CompletableFuture<R> result = new CompletableFuture<>();
            try {
                SwingUtilities.invokeAndWait(() -> {
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
