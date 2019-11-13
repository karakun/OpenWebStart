package com.openwebstart.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static BufferedImage resize(final BufferedImage source, final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D bg = image.createGraphics();
        final double sx = (double) width / source.getWidth();
        final double sy = (double) height / source.getHeight();

        bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        bg.scale(sx, sy);
        bg.drawImage(source, 0, 0, null);
        bg.dispose();
        return image;
    }
}
