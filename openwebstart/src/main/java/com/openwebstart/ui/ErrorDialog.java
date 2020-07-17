package com.openwebstart.ui;

import com.openwebstart.jvm.ui.Images;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;

public class ErrorDialog extends DialogWithDetails {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorDialog.class);

    private static JTextArea createStackTraceDetailsComponent(final Exception error) {
        Assert.requireNonNull(error, "error");

        final JTextArea exceptionDetailsArea = new JTextArea();
        exceptionDetailsArea.setEditable(false);
        exceptionDetailsArea.setRows(10);
        exceptionDetailsArea.setColumns(32);
        exceptionDetailsArea.setText(getStackTrace(error));
        return exceptionDetailsArea;
    }

    public ErrorDialog(final String message, final Exception error) {
        super(Translator.getInstance().translate("dialog.error.title"), new ImageIcon(Images.ERROR_64_URL), message, createStackTraceDetailsComponent(error));
    }

    private static String getStackTrace(final Exception exception) {
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
