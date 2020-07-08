package com.openwebstart.download;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

public class ApplicationDownloadDetailListRenderer extends JPanel implements ListCellRenderer<ApplicationDownloadResourceState> {

    private final JProgressBar progressBar;

    private final JLabel label;

    public ApplicationDownloadDetailListRenderer() {
        this.progressBar = new JProgressBar();
        this.label = new JLabel();
        setLayout(new BorderLayout(2, 2));
        add(label, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends ApplicationDownloadResourceState> list, final ApplicationDownloadResourceState value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        if (index % 2 == 0) {
            setBackground(new Color(240, 240, 240));
        } else {
            setBackground(null);
        }
        if (value == null) {
            progressBar.setVisible(false);
            label.setVisible(false);
        } else {
            progressBar.setVisible(true);
            label.setVisible(true);

            label.setText(value.getUrl() + " - " + value.getVersion());
            progressBar.setValue(value.getPercentage());
            progressBar.setToolTipText(value.getMessage());
        }
        return this;
    }
}
