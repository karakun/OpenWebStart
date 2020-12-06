package com.openwebstart.app.ui;

import com.openwebstart.app.Application;
import com.openwebstart.app.icon.ApplicationIconHandler;
import com.openwebstart.app.icon.IconDimensions;
import com.openwebstart.jvm.ui.Images;
import com.openwebstart.jvm.ui.dialogs.ByteUnit;
import com.openwebstart.ui.CenterLayout;
import com.openwebstart.ui.IconComponent;
import com.openwebstart.ui.ListHighlighter;
import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;

public class ApplicationListCellRenderer implements ListCellRenderer<Application> {

    private final Color BACKGROUND_EVEN = Color.WHITE;

    private final Color BACKGROUND_ODD = new Color(246, 246, 246);

    private final Color BACKGROUND_HOOVER = Color.CYAN.brighter();

    private final JPanel cellContent;

    private final JLabel titleLabel;

    private final JLabel detailsLabel;

    private final IconComponent actionsIcon;

    private final IconComponent actionsHooverIcon;

    private final ImageIcon appIcon;

    private final ListHighlighter<Application> listHighlighter;

    ApplicationListCellRenderer(final ListHighlighter<Application> listHighlighter) {
        this.listHighlighter = Assert.requireNonNull(listHighlighter, "listHighlighter");

        actionsIcon = new IconComponent(new ImageIcon(Images.MORE_OUTLINE_32_URL));
        actionsHooverIcon = new IconComponent(new ImageIcon(Images.MORE_32_URL));

        appIcon = new ImageIcon();

        titleLabel = new JLabel("VERSION");
        detailsLabel = new JLabel("ARCH");

        cellContent = new JPanel();
        cellContent.setLayout(LayoutFactory.createBorderLayout(12, 12));
        cellContent.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 8));

        final JPanel iconPanel = createIconPanel();
        final JPanel centerPanel = createCenterPanel();
        final JPanel actionWrapper = createActionPanel();

        cellContent.add(iconPanel, BorderLayout.WEST);
        cellContent.add(centerPanel, BorderLayout.CENTER);
        cellContent.add(actionWrapper, BorderLayout.EAST);
    }

    private JPanel createActionPanel() {
        final JPanel actionWrapper = new JPanel();
        actionWrapper.setBackground(null);
        actionWrapper.setLayout(new CenterLayout());
        actionWrapper.add(actionsIcon);
        actionWrapper.add(actionsHooverIcon);
        return actionWrapper;
    }

    private JPanel createCenterPanel() {
        titleLabel.setFont(titleLabel.getFont().deriveFont(22.0f));
        titleLabel.setMinimumSize(new Dimension(100, titleLabel.getPreferredSize().height));
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(10.0f).deriveFont(Font.ITALIC));
        detailsLabel.setForeground(Color.DARK_GRAY);

        final JPanel firstLinePanel = new JPanel();
        firstLinePanel.setBackground(null);
        firstLinePanel.setLayout(LayoutFactory.createBoxLayout(firstLinePanel, BoxLayout.LINE_AXIS));
        firstLinePanel.add(titleLabel);
        firstLinePanel.add(Box.createHorizontalGlue());

        final JPanel secondLine = new JPanel();
        secondLine.setBackground(null);
        secondLine.setLayout(LayoutFactory.createBoxLayout(secondLine, BoxLayout.LINE_AXIS));
        secondLine.add(detailsLabel);
        secondLine.add(Box.createHorizontalGlue());

        final JPanel centerPanel = new JPanel();
        centerPanel.setBackground(null);
        centerPanel.setLayout(LayoutFactory.createBoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(firstLinePanel);
        centerPanel.add(secondLine);
        centerPanel.add(Box.createVerticalGlue());

        return centerPanel;
    }

    private JPanel createIconPanel() {
        final IconComponent vmIcon = new IconComponent(appIcon);
        final JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new CenterLayout());
        iconPanel.setBackground(null);
        iconPanel.add(vmIcon);
        return iconPanel;
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends Application> list, final Application value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        titleLabel.setText(Optional.ofNullable(value).map(Application::getName).orElse(""));

        final long size = Optional.ofNullable(value).map(Application::getSize).orElse(0L);
        final ByteUnit byteUnit = ByteUnit.findBestUnit(size);
        detailsLabel.setText(String.format("%.0f", byteUnit.convertBytesToUnit(size)) + " " + byteUnit.getDecimalShortName());

        if (value != null) {
            appIcon.setImage(ApplicationIconHandler.getInstance().getIconOrDefault(value, IconDimensions.SIZE_64));
        }

        if (this.listHighlighter.getHoverIndex() == index) {
            cellContent.setBackground(BACKGROUND_HOOVER);
        } else {
            if (index % 2 == 0) {
                cellContent.setBackground(BACKGROUND_EVEN);
            } else {
                cellContent.setBackground(BACKGROUND_ODD);
            }
        }
        if (this.listHighlighter.isInActionArea() && this.listHighlighter.getHoverIndex() == index) {
            actionsHooverIcon.setVisible(true);
        } else {
            actionsHooverIcon.setVisible(false);
        }
        return cellContent;
    }
}
