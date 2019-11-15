package com.openwebstart.ui;

import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;

public class IconComponent extends JComponent {

    private final Icon icon;

    public IconComponent(final Icon icon) {
        this.icon = Assert.requireNonNull(icon, "icon");
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        icon.paintIcon(this, g, 0, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
