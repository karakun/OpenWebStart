package com.openwebstart.jvm.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Optional;

public class IconComponent extends JComponent {

    private Icon icon;

    public IconComponent() {
    }

    public IconComponent(final Icon icon) {
        this.icon = icon;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        Optional.ofNullable(icon).ifPresent(i -> i.paintIcon(this, g, 0, 0));
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(final Icon icon) {
        this.icon = icon;
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        final int w =  Optional.ofNullable(icon).map(i -> i.getIconWidth()).orElse(0);
        final int h = Optional.ofNullable(icon).map(i -> i.getIconHeight()).orElse(0);
        return new Dimension(w, h);
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
