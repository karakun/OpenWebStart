package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.ui.ErrorDialog;
import com.openwebstart.ui.ModalDialog;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DialogFactory {

    private static <R> R handleEdtConform(final DialogWithResult<R> dialog) {
        Assert.requireNonNull(dialog, "dialogHandler");

        final Supplier<R> dialogHandler = dialog::showAndWait;

        if(SwingUtils.isEventDispatchThread()) {
            final R result = dialogHandler.get();
            return Optional.ofNullable(result).orElseThrow(() -> new RuntimeException("Internal runtime error while handling dialog"));
        } else {
            try {
                final CompletableFuture<R> completableFuture = new CompletableFuture<>();
                SwingUtilities.invokeAndWait(() -> completableFuture.complete(dialogHandler.get()));
                return Optional.ofNullable(completableFuture.get()).orElseThrow(() -> new RuntimeException("Internal runtime error while handling dialog"));
            } catch (final Exception e) {
                throw new RuntimeException("Internal runtime error while handling dialog", e);
            }
        }
    }

    private static boolean handleYesNoDialogEdtConform(final String title, final String message) {
        return handleEdtConform(new YesNoDialog(title, message));
    }

    public static void showErrorDialog(final String message, final Exception error) {
        ErrorDialog.show(message, error);
    }

    public static boolean askForDeactivatedRuntimeUsage(final LocalJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");
        final Translator translator = Translator.getInstance();

        final String title = translator.translate("dialog.versionCheck.title");
        final String message = translator.translate("dialog.versionCheck.text", runtime.getVersion().toString());

        return handleYesNoDialogEdtConform(title, message);
    }

    public static boolean askForRuntimeUpdate(final RemoteJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");
        final Translator translator = Translator.getInstance();

            final String title = translator.translate("dialog.updateCheck.title");
            final String message = translator.translate("dialog.updateCheck.text", runtime.getVersion(), runtime.getVendor());

        return handleYesNoDialogEdtConform(title, message);
    }

    public static void showNotification(final String title, final String message) {
        final ModalDialog dialog = new ModalDialog();
        dialog.setTitle(title);
        final JLabel messageLabel = new JLabel(message);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        final JButton okButton = new JButton(Translator.getInstance().translate("action.ok"));
        okButton.addActionListener(e -> dialog.close());
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(ButtonPanelFactory.createButtonPanel(okButton), BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setResizable(false);
        dialog.showAndWait();
    }
}
