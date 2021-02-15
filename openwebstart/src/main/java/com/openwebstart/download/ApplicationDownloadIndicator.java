package com.openwebstart.download;

import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.jnlp.DownloadServiceListener;
import java.net.URL;

public class ApplicationDownloadIndicator implements DownloadIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDownloadIndicator.class);

    public static final ApplicationDownloadIndicator DOWNLOAD_INDICATOR = new ApplicationDownloadIndicator();

    private ApplicationDownloadIndicator() {
        // prevent creation of instances
    }

    @Override
    public DownloadServiceListener getListener(final String downloadName, final URL[] resources) {
        LOG.debug("DownloadServiceListener for {} will be created", downloadName);
        return new ApplicationDownloadDialog(downloadName);
    }

    @Override
    public void disposeListener(final DownloadServiceListener listener) {
        LOG.debug("DownloadServiceListener will be disposed");
        if (listener instanceof ApplicationDownloadDialog) {
            ApplicationDownloadDialog downloadDialog = (ApplicationDownloadDialog) listener;
            downloadDialog.close();
        }
    }
}
