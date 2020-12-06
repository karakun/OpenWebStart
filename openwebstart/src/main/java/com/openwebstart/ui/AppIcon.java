package com.openwebstart.ui;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppIcon {

    private static Image icon16;

    private static Image icon24;

    private static Image icon32;

    private static Image icon48;

    private static Image icon64;

    private static Image icon96;

    private static Image icon128;

    private static Image icon256;

    private static Image icon512;

    public static List<Image> getAllIcons() throws IOException {
        final List<Image> images = new ArrayList<>();
        images.add(getIcon16());
        images.add(getIcon24());
        images.add(getIcon32());
        images.add(getIcon48());
        images.add(getIcon64());
        images.add(getIcon96());
        images.add(getIcon128());
        images.add(getIcon256());
        images.add(getIcon512());
        return images;
    }

    public static synchronized Image getIcon16() throws IOException {
        if (icon16 != null) {
            icon16 = ImageIO.read(AppIcon.class.getResource("app-16.png"));
        }
        return icon16;
    }

    public static synchronized Image getIcon24() throws IOException {
        if (icon24 != null) {
            icon24 = ImageIO.read(AppIcon.class.getResource("app-24.png"));
        }
        return icon24;
    }

    public static synchronized Image getIcon32() throws IOException {
        if (icon32 != null) {
            icon32 = ImageIO.read(AppIcon.class.getResource("app-32.png"));
        }
        return icon32;
    }

    public static synchronized Image getIcon48() throws IOException {
        if (icon48 != null) {
            icon48 = ImageIO.read(AppIcon.class.getResource("app-48.png"));
        }
        return icon48;
    }

    public static synchronized Image getIcon64() throws IOException {
        if (icon64 != null) {
            icon64 = ImageIO.read(AppIcon.class.getResource("app-64.png"));
        }
        return icon64;
    }

    public static synchronized Image getIcon96() throws IOException {
        if (icon96 != null) {
            icon96 = ImageIO.read(AppIcon.class.getResource("app-96.png"));
        }
        return icon96;
    }

    public static synchronized Image getIcon128() throws IOException {
        if (icon128 != null) {
            icon128 = ImageIO.read(AppIcon.class.getResource("app-128.png"));
        }
        return icon128;
    }

    public static synchronized Image getIcon256() throws IOException {
        if (icon256 != null) {
            icon256 = ImageIO.read(AppIcon.class.getResource("app-256.png"));
        }
        return icon256;
    }

    public static synchronized Image getIcon512() throws IOException {
        if (icon512 != null) {
            icon512 = ImageIO.read(AppIcon.class.getResource("app-512.png"));
        }
        return icon512;
    }

}
