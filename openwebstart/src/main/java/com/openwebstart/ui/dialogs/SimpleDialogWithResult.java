package com.openwebstart.ui.dialogs;

import com.openwebstart.jvm.ui.Images;
import com.openwebstart.ui.IconComponent;
import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.util.List;

public class SimpleDialogWithResult<R> extends ButtonBasedDialogWithResult<R> {

    private final String message;

    private final ImageIcon imageIcon;

    public SimpleDialogWithResult(final String title, final String message, final DialogButton<R>... buttons) {
        this(title, message, ImageGallery.QUESTION.asImageIcon(), buttons);

    }

    public SimpleDialogWithResult(final String title, final String message, final ImageIcon imageIcon, final DialogButton<R>... buttons) {
        super(title, buttons);
        this.message = message;
        this.imageIcon = imageIcon;
    }

    protected JPanel createContentPane(final List<DialogButton<R>> buttons) {
        final IconComponent icon = new IconComponent(imageIcon);

        final JTextArea messageLabel = new JTextArea(message);
        messageLabel.setEditable(false);
        messageLabel.setBackground(null);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setLineWrap(true);
        messageLabel.setColumns(50);

        final JPanel messageWrapperPanel = new JPanel();
        messageWrapperPanel.setLayout(LayoutFactory.createBorderLayout(12, 12));
        messageWrapperPanel.add(icon, BorderLayout.WEST);
        messageWrapperPanel.add(messageLabel, BorderLayout.CENTER);

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(LayoutFactory.createBoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionWrapperPanel.add(Box.createHorizontalGlue());

        buttons.forEach(b -> {
            final JButton button = new JButton(b.getText());
            if (b.getDescription() != null) {
                button.setToolTipText(b.getDescription());
            }
            button.addActionListener(e -> {
                final R result = b.getOnAction().get();
                closeWithResult(result);
            });
            actionWrapperPanel.add(button);
        });

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(LayoutFactory.createBorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.add(messageWrapperPanel, BorderLayout.CENTER);
        contentPanel.add(actionWrapperPanel, BorderLayout.SOUTH);
        return contentPanel;
    }


    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final String msg1 = "This is a long text that should be displayed in more than 1 line. This is a long text that should be displayed in more than 1 line. This is a long text that should be displayed in more than 1 line.";
        final String msg2 = "Connection failed for URL: https://docs.oracle.com/javase/tutorialJWS/samples/uiswing/AccessibleScrollDemoProject/AccessibleScrollDemo.jnlp." +
                "\n\nDo you want to continue with no proxy or exit the application?";
        final DialogButton<Integer> exitButton = new DialogButton<>("Exit", () -> 0);

        new SimpleDialogWithResult<>("Title", msg1, exitButton).showAndWait();
        new SimpleDialogWithResult<>("Title", msg2, new ImageIcon(Images.NETWORK_64_URL), exitButton).showAndWait();
    }
}
