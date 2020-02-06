package com.openwebstart.ui.impl;

import com.openwebstart.ui.IconComponent;
import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.text.DefaultCaret;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Consumer;

public class Notification extends JWindow {

    public Notification(final String message, final boolean error, final Consumer<Notification> onClose) {
        Assert.requireNonBlank(message, "message");

        setAlwaysOnTop(true);

        final JEditorPane editorPane = new JEditorPane();
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        DefaultCaret caret = (DefaultCaret) editorPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(message);
        editorPane.setBackground(null);


        final IconComponent iconComponent;
        if (error) {
            iconComponent = new IconComponent(new ImageIcon(Notification.class.getResource("error-32.png")));
        } else {
            iconComponent = new IconComponent(new ImageIcon(Notification.class.getResource("info-32.png")));
        }
        final JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.PAGE_AXIS));
        Box iconBox = Box.createVerticalBox();
        iconBox.add(iconComponent);
        iconBox.add(Box.createVerticalGlue());
        iconPanel.add(iconBox);


        final IconComponent closeIconComponent = new IconComponent(new ImageIcon(Notification.class.getResource("close-highlight-16.png")));
        closeIconComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                Optional.ofNullable(onClose).ifPresent(c -> c.accept(Notification.this));
            }
        });
        final JPanel closeIconPanel = new JPanel();
        closeIconPanel.setLayout(new BoxLayout(closeIconPanel, BoxLayout.PAGE_AXIS));
        Box closeIconBox = Box.createVerticalBox();
        closeIconBox.add(closeIconComponent);
        closeIconBox.add(Box.createVerticalGlue());
        closeIconPanel.add(closeIconBox);

        final JPanel notificationPane = new JPanel(new BorderLayout(12, 12));
        notificationPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        notificationPane.add(editorPane, BorderLayout.CENTER);
        notificationPane.add(iconPanel, BorderLayout.WEST);
        notificationPane.add(closeIconPanel, BorderLayout.EAST);

        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        contentPane.add(notificationPane, BorderLayout.CENTER);

        setContentPane(contentPane);
    }
}
