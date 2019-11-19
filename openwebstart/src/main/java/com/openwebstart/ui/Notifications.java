package com.openwebstart.ui;

import com.openwebstart.ui.impl.Notification;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Notifications {

    private static final Logger LOG = LoggerFactory.getLogger(Notifications.class);

    private static final int PADDING = 18;

    private static final int NOTIFICATION_WIDTH = 320;

    private static final int NOTIFICATION_HEIGHT = 76;

    private static final int VISIBLE_IN_MS = 8_000;

    private static List<Notification> visibleNotifications = new ArrayList<>();

    private Notifications() {

    }

    public static void showError(final String message) {
        final Notification notification = new Notification(message, true, n -> hide(n));
        show(notification);
    }

    public static void showInfo(final String message) {
        final Notification notification = new Notification(message, false, n -> hide(n));
        show(notification);
    }

    private static void hide(final Notification notification) {
        notification.setVisible(false);
        notification.dispose();
        visibleNotifications.remove(notification);
        updateLayout();
    }

    private static void show(final Notification notification) {
        final Consumer<Notification> uiHandler = n -> {
            visibleNotifications.add(n);
            updateLayout();

            final Timer timer = new Timer(VISIBLE_IN_MS, e -> hide(n));
            timer.setRepeats(false);
            timer.setInitialDelay(VISIBLE_IN_MS);
            timer.start();
        };

        if(SwingUtilities.isEventDispatchThread()) {
            uiHandler.accept(notification);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> uiHandler.accept(notification));
            } catch (final Exception e) {
                LOG.error("Error while showing notification", e);
            }
        }
    }

    private static void updateLayout() {
        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final int screenWidth = gd.getDisplayMode().getWidth();

        int startY = PADDING;

        for (final Notification notification : visibleNotifications) {
            notification.setSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
            notification.setLocation(screenWidth - PADDING - NOTIFICATION_WIDTH, startY);
            startY = startY + NOTIFICATION_HEIGHT + PADDING;
            notification.setVisible(true);
        }
    }

    public static void main(String[] args) throws Exception{
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Hallo da draußen"));
        Thread.sleep(1_000);
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Dies ist ein langer Text der anzeigt das etwas passiert ist das nicht sein sollte."));
        Notifications.showInfo("Ha, jetzt kommen ganz viele Notifications.");
        Notifications.showError("Genau! Hier ist noch eine ;).");
        Notifications.showInfo("Dies ist ein langer Text der anzeigt das etwas passiert ist das nicht sein sollte. Und jetzt kommt sogar noch mehr text");
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Hallo da draußen"));
        Thread.sleep(6_000);
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Hallo da draußen"));
        Thread.sleep(1_000);
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Dies ist ein langer Text der anzeigt das etwas passiert ist das nicht sein sollte."));
        Thread.sleep(6_000);
    }
}
