package com.openwebstart.controlpanel;

import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class FormPanel extends JPanel {

    private static final int SEPERATOR_WIDTH = 6;

    public FormPanel() {
        super(new GridBagLayout());
    }

    public void addFlexibleRow(final int row) {
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridy = row;
        c2.weighty = 1;
        final JPanel panel = new JPanel();
        panel.setBackground(null);
        panel.setPreferredSize(new Dimension(0, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setMinimumSize(new Dimension(0, 0));
        add(panel, c2);
    }

    public void addRow(final int row, final JComponent component) {
        Assert.requireNonNull(component, "component");

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = row;
        c1.weightx = 3;
        c1.ipady = 20;
        c1.fill = GridBagConstraints.HORIZONTAL;
        add(component, c1);
    }

    public void addRow(final int row, final JComponent label, final JComponent editor) {
        Assert.requireNonNull(label, "label");
        Assert.requireNonNull(editor, "editor");

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = row;
        c1.ipady = 20;
        c1.fill = GridBagConstraints.HORIZONTAL;
        add(label, c1);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridy = row;
        final JPanel panel = new JPanel();
        panel.setBackground(null);
        panel.setPreferredSize(new Dimension(SEPERATOR_WIDTH, 1));
        panel.setMaximumSize(new Dimension(SEPERATOR_WIDTH, 1));
        panel.setMinimumSize(new Dimension(0, 0));
        add(panel, c2);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 2;
        c3.gridy = row;
        c3.weightx = 1;
        c3.fill = GridBagConstraints.HORIZONTAL;
        add(editor, c3);
    }

    public void addEditorRow(final int row, final JComponent editor) {
        Assert.requireNonNull(editor, "editor");

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 2;
        c2.gridy = row;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        add(editor, c2);
    }
}
