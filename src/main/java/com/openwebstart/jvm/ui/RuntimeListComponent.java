package com.openwebstart.jvm.ui;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.actions.Action;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class RuntimeListComponent extends JList<LocalJavaRuntime> {

    private final Function<LocalJavaRuntime, List<Action<LocalJavaRuntime>>> actionSupplier;

    public RuntimeListComponent(final Function<LocalJavaRuntime, List<Action<LocalJavaRuntime>>> actionSupplier) {
        this.actionSupplier = Objects.requireNonNull(actionSupplier);
        setCellRenderer(new RuntimeListCellRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                clickButtonAt(e.getPoint());
            }
        });
    }

    private void clickButtonAt(Point point) {
        final int index = locationToIndex(point);
        final LocalJavaRuntime item = getModel().getElementAt(index);
        final Rectangle bounds = getCellBounds(index, index);

        //check if point is on Button of cell....

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
