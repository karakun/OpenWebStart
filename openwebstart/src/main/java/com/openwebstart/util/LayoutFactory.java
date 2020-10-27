package com.openwebstart.util;

import javax.swing.*;
import java.awt.*;

public class LayoutFactory {

    private  LayoutFactory() {

    }

    /**
     *
     * @return <code>BorderLayout</code>
     */
    public static BorderLayout createBorderLayout() {
        return  new BorderLayout();
    }

    /**
     * Constructs a border layout with the specified gaps
     * between components.
     * @param vgap the vertical gap.
     * @param hgap the horizontal gap.
     * @return <code>BorderLayout</code> object
     */
    public static BorderLayout createBorderLayout(int vgap,int hgap) {
        return  new BorderLayout(vgap,hgap);
    }

    /**
     * Creates a layout manager that will lay out components along the
     * given axis.
     *
     * @param target  the container that needs to be laid out
     * @param axis  the axis to lay out components along. Can be one of:
     *              <code>BoxLayout.X_AXIS</code>,
     *              <code>BoxLayout.Y_AXIS</code>,
     *              <code>BoxLayout.LINE_AXIS</code> or
     *              <code>BoxLayout.PAGE_AXIS</code>
     *
     * @exception AWTError  if the value of <code>axis</code> is invalid
     */
    public static BoxLayout createBoxLayout(Container target,int axis) {return  new BoxLayout(target,axis);}



}
