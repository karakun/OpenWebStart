package com.openwebstart.jvm.ui.list;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.actions.Action;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class RuntimeListComponent extends JList<LocalJavaRuntime> {

    private final Function<LocalJavaRuntime, List<Action<LocalJavaRuntime>>> actionSupplier;

    private final RuntimeListHighlighter highlighter;

    private final RuntimeListModel model;

    public RuntimeListComponent(final Function<LocalJavaRuntime, List<Action<LocalJavaRuntime>>> actionSupplier) {
        this.actionSupplier = Objects.requireNonNull(actionSupplier);
        this.highlighter = new RuntimeListHighlighter(this);
        this.model = new RuntimeListModel();
        super.setModel(model);

        setCellRenderer(new RuntimeListCellRenderer(highlighter));
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {
                clickButtonAt(e.getPoint());
            }
        });
    }

    @Override
    public RuntimeListModel getModel() {
        return model;
    }

    @Override
    public void setModel(final ListModel<LocalJavaRuntime> model) {
        throw new RuntimeException("Cannot set model");
    }

    private void clickButtonAt(Point point) {
        if (highlighter.isInActionArea()) {
            final int index = locationToIndex(point);
            final LocalJavaRuntime item = getModel().getElementAt(index);
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
}
