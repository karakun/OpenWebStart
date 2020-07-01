package com.openwebstart.download;

import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.jnlp.DownloadServiceListener;
import java.net.URL;

public class ApplicationDownloadIndicator implements DownloadIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDownloadIndicator.class);

    @Override
    public DownloadServiceListener getListener(final String downloadName, final URL[] resources) {
        LOG.debug("ApplicationDownloadIndicator is used");
        return new ApplicationDownloadDialog(downloadName);
    }

    @Override
    public void disposeListener(final DownloadServiceListener listener) {
        if (listener instanceof ApplicationDownloadDialog) {
            ApplicationDownloadDialog downloadDialog = (ApplicationDownloadDialog) listener;
            downloadDialog.close();
        }
    }

    @Override
    public int getUpdateRate() {
        return 150;
    }

    @Override
    public int getInitialDelay() {
        return 300;
    }
}
