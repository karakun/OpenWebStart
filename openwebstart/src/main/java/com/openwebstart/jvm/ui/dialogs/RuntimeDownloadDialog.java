package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.http.DownloadType;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.ui.Images;
import com.openwebstart.ui.IconComponent;
import com.openwebstart.ui.ModalDialog;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class RuntimeDownloadDialog extends ModalDialog {

    public static void showDownloadDialog(final RemoteJavaRuntime remoteRuntime, final DownloadInputStream inputStream) {
        try {
            SwingUtilities.invokeLater(() -> {
                final RuntimeDownloadDialog downloadDialog = new RuntimeDownloadDialog(remoteRuntime, inputStream);
                downloadDialog.setVisible(true);
            });
        } catch (final Exception e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("jvmManager.error.download"), e);
        }
    }

    private RuntimeDownloadDialog(final RemoteJavaRuntime remoteRuntime, final DownloadInputStream inputStream) {
        Assert.requireNonNull(remoteRuntime, "remoteRuntime");
        Assert.requireNonNull(inputStream, "inputStream");

        final Translator translator = Translator.getInstance();

        setAlwaysOnTop(true);
        setTitle(translator.translate("dialog.jvmDownload.title"));
        setResizable(false);

        final JLabel messageLabel = new JLabel(translator.translate("dialog.jvmDownload.message", remoteRuntime.getVersion(), remoteRuntime.getVendor()));
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(320, progressBar.getPreferredSize().height));
        if (inputStream.getDownloadType() == DownloadType.INDETERMINATE) {
            progressBar.setIndeterminate(true);
        }
        final ImageIcon imageIcon = new ImageIcon(Images.NETWORK_64_URL);
        final IconComponent downloadIcon = new IconComponent(imageIcon);

        final JLabel progressLabel = new JLabel(translator.translate("dialog.jvmDownload.progressDefault"));

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
        inputStream.addDownloadDoneListener(e -> SwingUtilities.invokeLater(this::close));
        inputStream.addDownloadErrorListener(e -> SwingUtilities.invokeLater(this::close));
        inputStream.addDownloadPercentageListener(p -> SwingUtilities.invokeLater(() -> {
            if (inputStream.getDownloadType() == DownloadType.INDETERMINATE) {
                final long downloadSize = inputStream.getDownloaded();
                final ByteUnit unit = ByteUnit.findBestUnit(downloadSize);

                progressLabel.setText(String.format("%.0f", unit.convertBytesToUnit(downloadSize)) + " " + unit.getDecimalShortName());
            } else {
                progressBar.setValue((int) (p * 100.0));

                final long downloadSize = inputStream.getDownloaded();
                final ByteUnit downloadSizeUnit = ByteUnit.findBestUnit(downloadSize);

                final long completeSize = inputStream.getDataSize();
                final ByteUnit completeSizeUnit = ByteUnit.findBestUnit(completeSize);

                progressLabel.setText(String.format("%.0f", downloadSizeUnit.convertBytesToUnit(downloadSize)) + " " + downloadSizeUnit.getDecimalShortName() + " from " + String.format("%.2f", completeSizeUnit.convertBytesToUnit(completeSize)) + " " + completeSizeUnit.getDecimalShortName());
            }
        }));

        pack();
        setLocationRelativeTo(null);
    }
}
