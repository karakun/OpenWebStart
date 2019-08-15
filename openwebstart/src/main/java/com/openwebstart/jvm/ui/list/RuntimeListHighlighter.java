package com.openwebstart.jvm.ui.list;

import net.adoptopenjdk.icedteaweb.Assert;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RuntimeListHighlighter extends MouseAdapter {

    private int hoverIndex;

    private boolean inActionArea;

    private RuntimeListComponent listComponent;

    public RuntimeListHighlighter(final RuntimeListComponent listComponent) {
        this.listComponent = Assert.requireNonNull(listComponent, "listComponent");
        this.listComponent.addMouseListener(this);
        this.listComponent.addMouseMotionListener(this);

        hoverIndex = -1;
        inActionArea = false;
    }

    public int getHoverIndex() {
        return hoverIndex;
    }

    private void setHoverIndex(final int hoverIndex) {
        if(this.hoverIndex != hoverIndex) {
            final int oldIndex = this.hoverIndex;
            this.hoverIndex = hoverIndex;

            repaintIndex(oldIndex);
            repaintIndex(this.hoverIndex);
        }
    }

    private void repaintIndex(final int index) {
        final Rectangle bounds = listComponent.getCellBounds(index, index);
        if(bounds != null) {
            listComponent.repaint(bounds.x - 10, bounds.y - 10, bounds.width + 20, bounds.height + 20);
        }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        setInActionArea(false);
        setHoverIndex(-1);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        final int index = listComponent.locationToIndex(e.getPoint());
        setHoverIndex(index);

        final Rectangle bounds = listComponent.getCellBounds(index, index);

        if(bounds != null && e.getPoint().getX() >= bounds.width - 44 && e.getPoint().getY() >= bounds.y + (bounds.height / 2) - 22 && e.getPoint().getY() <= bounds.y + (bounds.height / 2) + 22 ) {
            setInActionArea(true);
        } else {
            setInActionArea(false);
        }
    }

    private void setInActionArea(final boolean inActionArea) {
        if(this.inActionArea != inActionArea) {
            this.inActionArea = inActionArea;
            repaintIndex(getHoverIndex());
        }
    }

    public boolean isInActionArea() {
        return inActionArea;
    }
}
