package com.openwebstart.controlpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.stream.Stream;

public class MaximumLayoutManager implements LayoutManager {
    @Override
    public void addLayoutComponent(final String name, final Component comp) {

    }

    @Override
    public void removeLayoutComponent(final Component comp) {

    }

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        final int width = Stream.of(parent.getComponents())
                .mapToInt(c -> c.getPreferredSize().width)
                .min().orElse(0);

        final int height = Stream.of(parent.getComponents())
                .mapToInt(c -> c.getPreferredSize().height)
                .min().orElse(0);

        return new Dimension(width, height);
    }

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        final int width = Stream.of(parent.getComponents())
                .mapToInt(c -> c.getMinimumSize().width)
                .min().orElse(0);

        final int height = Stream.of(parent.getComponents())
                .mapToInt(c -> c.getMinimumSize().height)
                .min().orElse(0);

        return new Dimension(width, height);
    }

    @Override
    public void layoutContainer(final Container parent) {
        Stream.of(parent.getComponents()).forEach(c -> {
            c.setLocation(0, 0);
            c.setSize(parent.getSize());
        });
    }
}
