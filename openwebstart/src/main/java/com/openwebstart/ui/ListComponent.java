package com.openwebstart.ui;

import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Function;

public class ListComponent<T> extends JList<T> {

    private final Function<T, List<Action<T>>> actionSupplier;

    private final ListHighlighter<T> highlighter;

    private final ListComponentModel<T> model;

    public ListComponent(final Function<T, List<Action<T>>> actionSupplier) {
        this.actionSupplier = Assert.requireNonNull(actionSupplier, "actionSupplier");
        this.highlighter = new ListHighlighter<>(this);
        this.model = new ListComponentModel<>();
        super.setModel(model);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {
                clickButtonAt(e.getPoint());
            }
        });
    }

    @Override
    public ListComponentModel<T> getModel() {
        return model;
    }

    @Override
    public void setModel(final ListModel<T> model) {
        throw new RuntimeException("Can not set model");
    }

    private void clickButtonAt(Point point) {
        if (highlighter.isInActionArea()) {
            final int index = locationToIndex(point);
            final T item = getModel().getElementAt(index);
            final JPopupMenu popupMenu = new JPopupMenu();
            actionSupplier.apply(item).forEach(a -> {
                final JMenuItem menuItem = new JMenuItem(a.getName());
                menuItem.setToolTipText(a.getDescription());
                menuItem.addActionListener(e -> a.call(item));
                menuItem.setEnabled(a.isActive());
                popupMenu.add(menuItem);
            });
            popupMenu.show(this, point.x, point.y);
        }
    }

    protected ListHighlighter<T> getHighlighter() {
        return highlighter;
    }
}
