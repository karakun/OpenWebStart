package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.ui.IconComponent;
import com.openwebstart.rico.http.DownloadInputStream;
import com.openwebstart.rico.http.DownloadType;
import com.openwebstart.rico.http.ByteUnit;
import net.adoptopenjdk.icedteaweb.Assert;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Objects;

public class RuntimeDownloadDialog extends JDialog {

    public RuntimeDownloadDialog(final RemoteJavaRuntime remoteRuntime,  final DownloadInputStream inputStream) {
        Assert.requireNonNull(remoteRuntime, "remoteRuntime");
        Assert.requireNonNull(inputStream, "inputStream");

        setAlwaysOnTop(true);
        setTitle("Download");
        setResizable(false);



        final JLabel messageLabel = new JLabel("Downloading runtime " + remoteRuntime.getVersion() + "-" + remoteRuntime.getVendor());
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(320, progressBar.getPreferredSize().height));
        if (Objects.equals(inputStream.getDownloadType(), DownloadType.INDETERMINATE)) {
            progressBar.setIndeterminate(true);
        }
        final IconComponent downloadIcon = new IconComponent(FontIcon.of(MaterialDesign.MDI_DOWNLOAD, 64, Color.DARK_GRAY));

        final JLabel progressLabel = new JLabel("0 KB from ?");


        final JPanel progressLabelWrapper = new JPanel();
        progressLabelWrapper.setLayout(new BorderLayout());
        progressLabelWrapper.add(progressLabel, BorderLayout.EAST);

        final JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout(4, 0));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 4));
        innerPanel.add(progressBar, BorderLayout.CENTER);
        innerPanel.add(progressLabelWrapper, BorderLayout.SOUTH);
        innerPanel.add(messageLabel, BorderLayout.NORTH);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.add(downloadIcon, BorderLayout.WEST);
        panel.add(innerPanel, BorderLayout.CENTER);
        add(panel);


        inputStream.setUpdateChunkSize(10_000);
        inputStream.addDownloadDoneListener(s -> close());
        inputStream.addDownloadErrorListener(e -> close());
        inputStream.addDownloadPercentageListener(p -> {
            if (Objects.equals(inputStream.getDownloadType(), DownloadType.INDETERMINATE)) {
                final long downloadSize = inputStream.getDownloaded();
                final ByteUnit unit = ByteUnit.findBestUnit(downloadSize);

                progressLabel.setText(String.format("%.0f", unit.convertBytesToUnit(downloadSize)) + " " + unit.getDecimalShortName());
            } else {
                progressBar.setValue((int) (p * 100.0));

                final long downloadSize = inputStream.getDownloaded();
                final ByteUnit downloadSizeUnit = ByteUnit.findBestUnit(downloadSize);

                final long completeSize = inputStream.getDataSize();
                final ByteUnit completeSizeUnit = ByteUnit.findBestUnit(completeSize);

                progressLabel.setText(String.format("%.0f", downloadSizeUnit.convertBytesToUnit(downloadSize)) + " " + downloadSizeUnit.getDecimalShortName() + " from " +  String.format("%.2f", completeSizeUnit.convertBytesToUnit(completeSize)) + " " + completeSizeUnit.getDecimalShortName());
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
