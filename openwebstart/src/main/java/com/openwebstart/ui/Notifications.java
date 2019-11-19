package com.openwebstart.ui;

import com.openwebstart.ui.impl.Notification;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

public class Notifications {

    private static final int PADDING = 18;

    private static final int NOTIFICATION_WIDTH = 320;

    private static final int NOTIFICATION_HEIGHT = 64;

    private static final int VISIBLE_IN_MS = 8_000;

    private static List<Notification> visibleNotifications = new ArrayList<>();

    private Notifications() {

    }

    public static void showError(final String messageKey) {
        final Notification notification = new Notification(Translator.getInstance().translate(messageKey), true, n -> hide(n));
        show(notification);
    }

    public static void showInfo(final String messageKey) {
        final Notification notification = new Notification(Translator.getInstance().translate(messageKey), false, n -> hide(n));
        show(notification);
    }

    private static void hide(final Notification notification) {
        notification.setVisible(false);
        notification.dispose();
        visibleNotifications.remove(notification);
        updateLayout();
    }

    private static void show(final Notification notification) {
        visibleNotifications.add(notification);
        updateLayout();

        final Timer timer = new Timer(VISIBLE_IN_MS, e -> hide(notification));
        timer.setRepeats(false);
        timer.setInitialDelay(VISIBLE_IN_MS);
        timer.start();
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
        SwingUtilities.invokeAndWait(() -> Notifications.showInfo("Ha, jetzt kommen ganz viele Notifications."));
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Genau! Hier ist noch eine ;)."));
        SwingUtilities.invokeAndWait(() -> Notifications.showInfo("Dies ist ein langer Text der anzeigt das etwas passiert ist das nicht sein sollte. Und jetzt kommt sogar noch mehr text"));
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Hallo da draußen"));
        Thread.sleep(6_000);
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Hallo da draußen"));
        Thread.sleep(1_000);
        SwingUtilities.invokeAndWait(() -> Notifications.showError("Dies ist ein langer Text der anzeigt das etwas passiert ist das nicht sein sollte."));
        Thread.sleep(6_000);
    }
}
