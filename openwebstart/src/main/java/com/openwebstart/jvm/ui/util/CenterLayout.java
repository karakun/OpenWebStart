package com.openwebstart.jvm.ui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class CenterLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(final String name, final Component comp) {}

    @Override
    public void removeLayoutComponent(final Component comp) {}

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        final int width = Arrays.asList(parent.getComponents()).stream()
                .map(c -> c.getPreferredSize().width)
                .sorted((a, b) -> Integer.compare(b, a))
                .findFirst().orElse(-1);

        final int height = Arrays.asList(parent.getComponents()).stream()
                .map(c -> c.getPreferredSize().height)
                .sorted((a, b) -> Integer.compare(b, a))
                .findFirst().orElse(-1);

        return new Dimension(width, height);
    }

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(final Container parent) {
        Arrays.asList(parent.getComponents()).stream().forEach(c -> {
            final Dimension containerSize = parent.getSize();
            final Dimension prefSize = c.getPreferredSize();
            c.setLocation((containerSize.width - prefSize.width)/2, (containerSize.height - prefSize.height)/2);
            c.setSize(prefSize);
        });
    }
}
