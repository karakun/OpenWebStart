package com.openwebstart.ui;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class DialogWithDetails extends ModalDialog {

    public DialogWithDetails(final String title, final ImageIcon icon, final String message, final JComponent detailsContent) {
        setTitle(title);

        final Translator translator = Translator.getInstance();
        final IconComponent downloadIcon = new IconComponent(icon);
        final JLabel messageLabel = new JLabel(message);
        final JScrollPane scrollPane = new JScrollPane(detailsContent);

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
            if (!scrollPane.isVisible()) {
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
}
