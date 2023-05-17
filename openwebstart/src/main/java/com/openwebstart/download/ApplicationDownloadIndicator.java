package com.openwebstart.download;

import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.jnlp.DownloadServiceListener;
import javax.swing.SwingUtilities;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;

import static com.openwebstart.concurrent.ThreadPoolHolder.getDaemonExecutorService;

public class ApplicationDownloadIndicator implements DownloadIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDownloadIndicator.class);
    private static final int GRACE_PERIOD = 1_500;

    public static final ApplicationDownloadIndicator DOWNLOAD_INDICATOR = new ApplicationDownloadIndicator();

    private final ReentrantLock lock = new ReentrantLock();
    private ApplicationDownloadDialog dialog;
    private int counter = 0;

    private ApplicationDownloadIndicator() {
        // prevent creation of instances
    }

    @Override
    public DownloadServiceListener getListener(final String downloadName, final URL[] resources) {
        lock.lock();
        try {
            LOG.debug("DownloadServiceListener for {} will be created", downloadName);
            counter++;
            if (dialog == null) {
                dialog = new ApplicationDownloadDialog(downloadName);
            } else {
                dialog.setApplicationName(downloadName);
            }
            return dialog;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void disposeListener(final DownloadServiceListener listener) {
        if (listener == dialog) {
            LOG.debug("DownloadServiceListener will be disposed");
            lock.lock();
            try {
                counter--;
                getDaemonExecutorService().submit(this::closeAfterGracePeriod);
            } finally {
                lock.unlock();
            }
        } else {
            LOG.error("dispose called with unknown listener: {}", listener);
        }
    }

    private void closeAfterGracePeriod() {
        try {
            Thread.sleep(GRACE_PERIOD);
        } catch (InterruptedException e) {
            LOG.warn("grace period for closing dialog has been interrupted. close immediately");
        }

        SwingUtilities.invokeLater(() -> {
            lock.lock();
            try {
                if (dialog != null && counter == 0) {
                    LOG.debug("Closing DownloadServiceListener");
                    dialog.close();
                    dialog = null;
                }
            } catch (Exception e) {
            } finally {
                lock.unlock();
            }
        });
    }
}
