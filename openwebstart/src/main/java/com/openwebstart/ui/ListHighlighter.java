package com.openwebstart.ui;

import net.adoptopenjdk.icedteaweb.Assert;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ListHighlighter<T> extends MouseAdapter {

    private static final int ACTION_AREA_WIDTH = 44;

    private static final int ACTION_AREA_HALF_OF_HEIGHTS = ACTION_AREA_WIDTH / 2;

    private int hoverIndex;

    private boolean inActionArea;

    private ListComponent<T> listComponent;

    public ListHighlighter(final ListComponent<T> listComponent) {
        this.listComponent = Assert.requireNonNull(listComponent, "listComponent");
        this.listComponent.addMouseListener(this);
        this.listComponent.addMouseMotionListener(this);

        hoverIndex = -1;
        inActionArea = false;
    }

    public int getHoverIndex() {
        return hoverIndex;
    }

    public boolean isInActionArea() {
        return inActionArea;
    }

    private void setHoverIndex(final int hoverIndex) {
        if (this.hoverIndex != hoverIndex) {
            final int oldIndex = this.hoverIndex;
            this.hoverIndex = hoverIndex;

            repaintIndex(oldIndex);
            repaintIndex(this.hoverIndex);
        }
    }

    private void repaintIndex(final int index) {
        final Rectangle bounds = listComponent.getCellBounds(index, index);
        if (bounds != null) {
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

        if (bounds == null) {
            setInActionArea(false);
        } else {
            final int halfOfRowHeight = bounds.height / 2;
            final int verticalMiddleOfRow = bounds.y + halfOfRowHeight;

            final boolean isInActionArea = e.getPoint().getX() >= bounds.width - ACTION_AREA_WIDTH
                    && e.getPoint().getY() >= verticalMiddleOfRow - ACTION_AREA_HALF_OF_HEIGHTS
                    && e.getPoint().getY() <= verticalMiddleOfRow + ACTION_AREA_HALF_OF_HEIGHTS;

            setInActionArea(isInActionArea);
        }
    }

    private void setInActionArea(final boolean inActionArea) {
        if (this.inActionArea != inActionArea) {
            this.inActionArea = inActionArea;
            repaintIndex(getHoverIndex());
        }
    }
}
