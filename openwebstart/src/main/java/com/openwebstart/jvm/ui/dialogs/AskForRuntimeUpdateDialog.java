package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.ui.Images;
import com.openwebstart.jvm.ui.util.IconComponent;
import net.adoptopenjdk.icedteaweb.Assert;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AskForRuntimeUpdateDialog extends JDialog {

    public static boolean askForUpdate(final RemoteJavaRuntime remoteJavaRuntime) {
        try {
            final CompletableFuture<Boolean> result = new CompletableFuture<>();
            SwingUtilities.invokeLater(() -> {
                final AskForRuntimeUpdateDialog dialog = new AskForRuntimeUpdateDialog(remoteJavaRuntime);
                final boolean update = dialog.showAndWait();
                result.complete(update);
            });
            return result.get();
        } catch (final Exception e) {
            SwingUtilities.invokeLater(() -> new ErrorDialog("Error while asking for update", e).showAndWait());
            return true;
        }
    }

    private final AtomicBoolean result = new AtomicBoolean(false);

    private AskForRuntimeUpdateDialog(final RemoteJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        final ImageIcon imageIcon = new ImageIcon(Images.QUESTION_64_URL);
        final IconComponent downloadIcon = new IconComponent(imageIcon);

        final JLabel messageLabel = new JLabel("A new Java runtime (version '" + runtime.getVersion() + "' / vendor '" + runtime.getVendor() + "') is available. Do you want to download this version?");

        final JButton noUpdateButton = new JButton("No");
        noUpdateButton.addActionListener(e -> close(false));
        final JButton updateButton = new JButton("Yes");
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

        actionWrapperPanel.setLayout(new BoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionWrapperPanel.add(Box.createHorizontalGlue());
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

    private boolean showAndWait() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        return result.get();
    }
}
