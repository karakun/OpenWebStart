package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;

import javax.swing.SwingUtilities;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DialogFactory {

    private static <R> R handleEdtConform(final DialogWithResult<R> dialog) {
        Assert.requireNonNull(dialog, "dialogHandler");

        final Supplier<R> dialogHandler = () -> dialog.showAndWait();

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

    public static boolean askForDeactivatedRuntimeUsage(final LocalJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");

        final String title = "Version check";
        final String message = "Java version " + runtime.getVersion().toString() + " is already installed on your system but deactiveated. Should it be used?";

        return handleYesNoDialogEdtConform(title, message);
    }

    public static boolean askForRuntimeUpdate(final RemoteJavaRuntime runtime) {
        Assert.requireNonNull(runtime, "runtime");

            final String title = "Runtime Update";
            final String message = "A new Java runtime (version '" + runtime.getVersion() + "' / vendor '" + runtime.getVendor() + "') is available. Do you want to download this version?";

        return handleYesNoDialogEdtConform(title, message);
    }

}
