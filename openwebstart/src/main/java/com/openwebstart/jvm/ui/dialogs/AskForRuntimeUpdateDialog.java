package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.ui.IconComponent;
import net.adoptopenjdk.icedteaweb.Assert;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.concurrent.atomic.AtomicBoolean;

public class AskForRuntimeUpdateDialog extends JDialog {

    private final AtomicBoolean result = new AtomicBoolean(false);

    public AskForRuntimeUpdateDialog(final RemoteJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        final IconComponent downloadIcon = new IconComponent(FontIcon.of(MaterialDesign.MDI_INFORMATION, 64, Color.RED));

        final JLabel messageLabel = new JLabel("A new Java runtime (version '" + runtime.getVersion() + "' / vendor '" + runtime.getVendor() +"') is available. Do you want to download this version?");

        final JButton noUpdateButton = new JButton("no");
        noUpdateButton.addActionListener(e -> close(false));
        final JButton updateButton = new JButton("yes");
        updateButton.addActionListener(e -> close(true));

        final JPanel messageWrapperPanel = new JPanel();
        messageWrapperPanel.setLayout(new BorderLayout(12, 12));
        messageWrapperPanel.add(downloadIcon, BorderLayout.WEST);
        messageWrapperPanel.add(messageLabel, BorderLayout.CENTER);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(messageWrapperPanel, BorderLayout.NORTH);

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actionWrapperPanel.add(noUpdateButton);
        actionWrapperPanel.add(updateButton);

        panel.add(actionWrapperPanel, BorderLayout.SOUTH);
        add(panel);
    }

    private void close(final boolean result) {
        this.result.set(result);
        this.setVisible(false);
        this.dispose();
    }

    public boolean showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        return result.get();
    }
}
