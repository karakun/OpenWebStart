package com.openwebstart.jvm.ui.list;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.Images;
import com.openwebstart.jvm.ui.util.CenterLayout;
import com.openwebstart.jvm.ui.util.IconComponent;
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

class RuntimeListCellRenderer implements ListCellRenderer<LocalJavaRuntime> {

    private final Color BACKGROUND_EVEN = Color.WHITE;

    private final Color BACKGROUND_ODD = new Color(246, 246, 246);

    private final Color BACKGROUND_HOOVER = Color.CYAN.brighter();

    private final JPanel cellContent;

    private final JLabel versionLabel;

    private final JLabel vendorLabel;

    private final JLabel archLabel;

    private final JLabel javaHomeLabel;

    private final IconComponent deactivatedIcon;

    private final IconComponent actionsIcon;

    private final IconComponent actionsHooverIcon;

    private final RuntimeListHighlighter listHighlighter;

    RuntimeListCellRenderer(final RuntimeListHighlighter listHighlighter) {
        this.listHighlighter = Assert.requireNonNull(listHighlighter, "listHighlighter");

        actionsIcon = new IconComponent(new ImageIcon(Images.MORE_OUTLINE_32_URL));
        actionsHooverIcon = new IconComponent(new ImageIcon(Images.MORE_32_URL));


        versionLabel = new JLabel("VERSION");
        vendorLabel = new JLabel("VENDOR");
        archLabel = new JLabel("ARCH");
        javaHomeLabel = new JLabel("JAVA_HOME");
        deactivatedIcon = new IconComponent(new ImageIcon(Images.DEACTIVATED_24_URL));


        cellContent = new JPanel();
        cellContent.setLayout(new BorderLayout(12, 12));
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
        versionLabel.setFont(versionLabel.getFont().deriveFont(22.0f));
        versionLabel.setMinimumSize(new Dimension(100, versionLabel.getPreferredSize().height));
        vendorLabel.setFont(vendorLabel.getFont().deriveFont(22.0f).deriveFont(Font.ITALIC));
        archLabel.setFont(archLabel.getFont().deriveFont(10.0f).deriveFont(Font.ITALIC));
        archLabel.setForeground(Color.DARK_GRAY);
        javaHomeLabel.setFont(javaHomeLabel.getFont().deriveFont(10.0f));
        javaHomeLabel.setForeground(Color.DARK_GRAY);

        final JPanel firstLinePanel = new JPanel();
        firstLinePanel.setBackground(null);
        firstLinePanel.setLayout(new BoxLayout(firstLinePanel, BoxLayout.LINE_AXIS));
        firstLinePanel.add(versionLabel);
        firstLinePanel.add(Box.createHorizontalStrut(6));
        firstLinePanel.add(vendorLabel);
        firstLinePanel.add(Box.createHorizontalGlue());

        final JPanel secondLine = new JPanel();
        secondLine.setBackground(null);
        secondLine.setLayout(new BoxLayout(secondLine, BoxLayout.LINE_AXIS));
        secondLine.add(archLabel);
        secondLine.add(Box.createHorizontalStrut(6));
        secondLine.add(javaHomeLabel);
        secondLine.add(Box.createHorizontalGlue());

        final JPanel centerPanel = new JPanel();
        centerPanel.setBackground(null);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(firstLinePanel);
        centerPanel.add(secondLine);
        centerPanel.add(Box.createVerticalGlue());

        return centerPanel;
    }

    private JPanel createIconPanel() {
        final IconComponent vmIcon = new IconComponent(new ImageIcon(Images.VMCUBE_64_URL));

        final JPanel deactivatedIconWrapper = new JPanel();
        deactivatedIconWrapper.setLayout(null);
        deactivatedIconWrapper.setBackground(new Color(0, 0, 0, 0));
        deactivatedIconWrapper.setPreferredSize(new Dimension(64, 64));
        deactivatedIconWrapper.setMinimumSize(new Dimension(64, 64));
        deactivatedIconWrapper.add(deactivatedIcon);
        deactivatedIcon.setLocation(40, 40);
        deactivatedIcon.setSize(24, 24);

        final JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new CenterLayout());
        iconPanel.setBackground(null);
        iconPanel.add(deactivatedIconWrapper);
        iconPanel.add(vmIcon);
        return iconPanel;
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends LocalJavaRuntime> list, final LocalJavaRuntime value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        versionLabel.setText(Optional.ofNullable(value).map(v -> v.getVersion().toString()).orElse("unknown version"));
        vendorLabel.setText(Optional.ofNullable(value).map(v -> v.getVendor().getName()).orElse("unknown vendor"));
        archLabel.setText(Optional.ofNullable(value).map(v -> v.getOperationSystem().getName()).orElse("unknown operating system"));
        javaHomeLabel.setText(Optional.ofNullable(value).map(this::getJavaHome).orElse("unknown location"));

        if (this.listHighlighter.getHoverIndex() == index) {
            cellContent.setBackground(BACKGROUND_HOOVER);
        } else {
            if (index % 2 == 0) {
                cellContent.setBackground(BACKGROUND_EVEN);
            } else {
                cellContent.setBackground(BACKGROUND_ODD);
            }
        }

        if (Optional.ofNullable(value).map(LocalJavaRuntime::isActive).orElse(false)) {
            deactivatedIcon.setVisible(false);
        } else {
            deactivatedIcon.setVisible(true);
        }

        if (this.listHighlighter.isInActionArea() && this.listHighlighter.getHoverIndex() == index) {
            actionsHooverIcon.setVisible(true);
        } else {
            actionsHooverIcon.setVisible(false);
        }

        return cellContent;
    }

    private String getJavaHome(LocalJavaRuntime v) {
        if (v.isManaged()) {
            return "";
        }
        return v.getJavaHome().toString();
    }
}
