package com.openwebstart.jvm.ui;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.os.OperationSystem;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Objects;
import java.util.Optional;

public class RuntimeListCellRenderer implements ListCellRenderer<LocalJavaRuntime> {

    private final JPanel cellContent;

    private final IconComponent osIcon;

    private final JLabel versionLabel;

    public RuntimeListCellRenderer() {
        cellContent = new JPanel();
        cellContent.setLayout(new BorderLayout(12, 12));

        osIcon = new IconComponent();
        versionLabel = new JLabel();
        final IconComponent actionsIcon = new IconComponent(FontIcon.of(MaterialDesign.MDI_DOTS_HORIZONTAL, 32, Color.BLUE));


        cellContent.add(osIcon, BorderLayout.WEST);
        cellContent.add(versionLabel, BorderLayout.CENTER);
        cellContent.add(actionsIcon, BorderLayout.EAST);

        cellContent.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends LocalJavaRuntime> list, final LocalJavaRuntime value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        versionLabel.setText(Optional.ofNullable(value).map(v -> v.getVersion()).orElse("UNKNOWN"));
        final Ikon icon = Optional.ofNullable(value).map(v -> v.getOperationSystem()).map(os -> {
            if (Objects.equals(os, OperationSystem.MAC64)) {
                return MaterialDesign.MDI_APPLE;
            } else if (Objects.equals(os, OperationSystem.WIN32) || Objects.equals(os, OperationSystem.WIN64)) {
                return MaterialDesign.MDI_WINDOWS;
            }
            return MaterialDesign.MDI_DO_NOT_DISTURB;
        }).orElse(MaterialDesign.MDI_DO_NOT_DISTURB);
        osIcon.setIcon(FontIcon.of(icon, 32, Color.DARK_GRAY));

        if (!isSelected) {
            if(index%2 == 0) {
                cellContent.setBackground(Color.WHITE);
            } else {
                cellContent.setBackground(new Color(246, 246, 246));
            }
        } else {
            cellContent.setBackground(Color.CYAN);
        }

        if(Optional.ofNullable(value).map(v -> v.isActive()).orElse(false)) {
            versionLabel.setForeground(Color.BLACK);
        } else {
            versionLabel.setForeground(Color.LIGHT_GRAY);
        }


        return cellContent;
    }
}
