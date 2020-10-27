package com.openwebstart.controlpanel;

import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

public class ButtonPanelFactory {

    public static JPanel createButtonPanel(final JButton... buttons) {
        return createButtonPanel(Arrays.asList(buttons));
    }

    public static JPanel createButtonPanel(final List<JButton> buttons) {
        Assert.requireNonNull(buttons, "buttons");

        final JPanel actionWrapperPanel = new JPanel();
        actionWrapperPanel.setBackground(Color.WHITE);
        actionWrapperPanel.setLayout(LayoutFactory.createBoxLayout(actionWrapperPanel, BoxLayout.LINE_AXIS));
        actionWrapperPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        actionWrapperPanel.add(Box.createHorizontalGlue());
        buttons.forEach(actionWrapperPanel::add);

        final JPanel topBorder = new JPanel();
        topBorder.setBackground(Color.GRAY);
        topBorder.setPreferredSize(new Dimension(1, 1));
        topBorder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        topBorder.setMinimumSize(new Dimension(1, 1));

        final JPanel actionPanel = new JPanel(LayoutFactory.createBorderLayout());
        actionPanel.add(topBorder, BorderLayout.NORTH);
        actionPanel.add(actionWrapperPanel, BorderLayout.CENTER);
        return actionPanel;
    }
}
