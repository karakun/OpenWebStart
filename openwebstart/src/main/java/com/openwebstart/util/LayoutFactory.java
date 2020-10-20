package com.openwebstart.util;

import java.awt.*;

public class LayoutFactory {

    private  LayoutFactory() {

    }

    /**
     *
     * @return <code>BorderLayout</code>
     */
    public static BorderLayout getBorderLayout() {
        return  new BorderLayout();
    }

    /**
     * Constructs a border layout with the specified gaps
     * between components.
     * @param vgap the vertical gap.
     * @param hgap the horizontal gap.
     * @return <code>BorderLayout</code> object
     */
    public static BorderLayout getBorderLayout(int vgap,int hgap) {
        return  new BorderLayout(vgap,hgap);
    }



}
