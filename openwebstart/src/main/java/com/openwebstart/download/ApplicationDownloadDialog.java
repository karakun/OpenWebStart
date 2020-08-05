package com.openwebstart.download;

import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.controlpanel.MaximumLayoutManager;
import com.openwebstart.jvm.ui.dialogs.ByteUnit;
import com.openwebstart.ui.ErrorDialog;
import com.openwebstart.ui.ModalDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.ImageResources;

import javax.jnlp.DownloadServiceListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ApplicationDownloadDialog extends ModalDialog implements DownloadServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDownloadDialog.class);

    private final Map<URL, ApplicationDownloadResourceState> resourceStates;

    private final Lock resourceStatesLock = new ReentrantLock();

    private int lastOverallPercent = -1;

    private final Lock lastOverallPercentLock = new ReentrantLock();

    private final JProgressBar overallProgressBar;

    private final ApplicationDownloadDetailListModel listModel;

    public ApplicationDownloadDialog(final String applicationName) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        final Translator translator = Translator.getInstance();


        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                final String[] options = {translator.translate("appDownload.exit.yes"), translator.translate("appDownload.exit.no")};
                final int result = JOptionPane.showOptionDialog(ApplicationDownloadDialog.this,
                        translator.translate("appDownload.exit.text", applicationName), translator.translate("appDownload.exit.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                if (result == 0) {
                    System.exit(0);
                }
            }
        });

        setTitle(translator.translate("appDownload.title"));
        setIconImages(ImageResources.INSTANCE.getApplicationImages());
        resourceStates = new HashMap<>();

        final FormPanel mainPanel = new FormPanel();
        final JLabel messageLabel = new JLabel(translator.translate("appDownload.message", applicationName));
        overallProgressBar = new JProgressBar();
        overallProgressBar.setIndeterminate(true);

        listModel = new ApplicationDownloadDetailListModel();
        final JList<ApplicationDownloadResourceState> detailsList = new JList<>();
        detailsList.setModel(listModel);
        detailsList.setCellRenderer(new ApplicationDownloadDetailListRenderer());

        final JScrollPane scrollPane = new JScrollPane(detailsList);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        scrollPane.setMinimumSize(new Dimension(0, 0));

        final JPanel scrollPaneWrapper = new JPanel(new MaximumLayoutManager());
        scrollPaneWrapper.setBackground(null);

        final JButton showDetails = new JButton(translator.translate("action.showDetails"));
        showDetails.addActionListener(e -> {
            if (!Arrays.asList(scrollPaneWrapper.getComponents()).contains(scrollPane)) {
                scrollPaneWrapper.add(scrollPane);
                showDetails.setText(translator.translate("action.hideDetails"));
            } else {
                scrollPaneWrapper.remove(scrollPane);
                showDetails.setText(translator.translate("action.showDetails"));
            }
            this.pack();
        });
        final JPanel buttonPane = new JPanel(new BorderLayout());
        buttonPane.add(showDetails, BorderLayout.EAST);

        mainPanel.addRow(0, messageLabel);
        mainPanel.addRow(1, overallProgressBar);


        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridy = 2;
        c2.weighty = 1;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPaneWrapper, c2);

        mainPanel.addRow(3, buttonPane);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        setContentPane(mainPanel);
    }

    private void onUpdate(final int overallPercent) {
        lastOverallPercentLock.lock();
        try {
            if (lastOverallPercent != overallPercent) {
                lastOverallPercent = overallPercent;
                SwingUtilities.invokeLater(() -> updateUi(lastOverallPercent));
            }
        } finally {
            lastOverallPercentLock.unlock();
        }
    }

    private void onUpdate(final URL url, final ApplicationDownloadResourceState resourceState) {
        resourceStatesLock.lock();
        try {
            ApplicationDownloadResourceState lastState = resourceStates.get(url);
            if (lastState == null
                    || resourceState.getPercentage() != lastState.getPercentage()
                    || !Objects.equals(resourceState.getDownloadState(), lastState.getDownloadState())) {
                resourceStates.put(url, resourceState);
                SwingUtilities.invokeLater(() -> updateUi(url, resourceState));
            }
        } finally {
            resourceStatesLock.unlock();
        }
    }

    private void updateUi(final URL url, final ApplicationDownloadResourceState resourceState) {
        if (!isVisible()) {
            showAndWait();
        } else {
            if (resourceState.getPercentage() >= 100) {
                listModel.remove(resourceState);
            } else {
                listModel.add(resourceState);
            }
        }
    }

    private void updateUi(final int overallPercent) {
        if (!isVisible()) {
            showAndWait();
        } else {
            if (overallPercent > 0) {
                overallProgressBar.setIndeterminate(false);
                overallProgressBar.setValue(overallPercent);
                overallProgressBar.setToolTipText(overallPercent + " %");
            } else {
                overallProgressBar.setIndeterminate(true);
            }
        }
    }

    private int getPercentage(final long total, final long current) {
        if (total <= 0) {
            return -1;
        }
        if (current < 0) {
            return -1;
        }
        final long percentageInLong = Math.min(100l, current / (total / 100l));
        return (int) percentageInLong;
    }

    /**
     * A JNLP client's DownloadService implementation should call this method several times during a download.
     * A DownloadServiceListener implementation may display a progress bar and / or update information based on the parameters.
     *
     * @param url            The URL representing the resource being downloaded.
     * @param version        The version of the resource being downloaded.
     * @param readSoFar      The number of bytes downloaded so far.
     * @param total          The total number of bytes to be downloaded, or -1 if the number is unknown.
     * @param overallPercent The percentage of the overall update operation that is complete, or -1 if the percentage is unknown.
     */
    @Override
    public void progress(final URL url, final String version, final long readSoFar, final long total, final int overallPercent) {
        //TODO: We need LOG.trace ...
        //LOG.debug("Download Listener receives progress update for {} - {} / {} - {}", url.getFile(), readSoFar, total, overallPercent);

        final ByteUnit readSoFarUnit = ByteUnit.findBestUnit(readSoFar);
        final ByteUnit totalUnit = ByteUnit.findBestUnit(total);
        final Translator translator = Translator.getInstance();

        final String message = translator.translate("appDownload.state.download.message", url, version, readSoFarUnit.convertBytesToUnit(readSoFar), readSoFarUnit.getDecimalShortName(), totalUnit.convertBytesToUnit(total), totalUnit.getDecimalShortName());
        final int percentage = getPercentage(total, readSoFar);


        final ApplicationDownloadResourceState resourceState = new ApplicationDownloadResourceState(url, version, message, percentage, ApplicationDownloadState.DOWNLOADING);
        onUpdate(url, resourceState);

        onUpdate(overallPercent);
    }

    /**
     * A JNLP client's DownloadService implementation should call this method at least several times during validation
     * of a download. Validation often includes ensuring that downloaded resources are authentic (appropriately signed).
     * A DownloadServiceListener implementation may display a progress bar and / or update information based on the parameters.
     *
     * @param url            The URL representing the resource being validated.
     * @param version        The version of the resource being validated.
     * @param entry          The number of JAR entries validated so far.
     * @param total          The total number of entries to be validated.
     * @param overallPercent The percentage of the overall update operation that is complete, or -1 if the percentage is unknown.
     */
    @Override
    public void validating(final URL url, final String version, final long entry, final long total, final int overallPercent) {
        //TODO: We need LOG.trace ...
        //LOG.debug("Download Listener receives validation update");
        final Translator translator = Translator.getInstance();
        final String message = translator.translate("appDownload.state.validation.message", url, version, entry, total);
        final int percentage = getPercentage(total, entry);

        final ApplicationDownloadResourceState resourceState = new ApplicationDownloadResourceState(url, version, message, percentage, ApplicationDownloadState.DOWNLOADING);
        onUpdate(url, resourceState);

        onUpdate(overallPercent);
    }

    /**
     * A JNLP client's DownloadService implementation should call this method at least several times when applying an
     * incremental update to an in-cache resource. A DownloadServiceListener implementation may display a progress bar
     * and / or update information based on the parameters.
     *
     * @param url            The URL representing the resource being patched.
     * @param version        The version of the resource being patched.
     * @param patchPercent   The percentage of the patch operation that is complete, or -1 if the percentage is unknown.
     * @param overallPercent The percentage of the overall update operation that is complete, or -1 if the percentage is unknown.
     */
    @Override
    public void upgradingArchive(final URL url, final String version, final int patchPercent, final int overallPercent) {
        //TODO: We need LOG.trace ...
        //LOG.debug("Download Listener receives patching update");
        final Translator translator = Translator.getInstance();
        final String message = translator.translate("appDownload.state.patching.message", url, version, patchPercent);
        final ApplicationDownloadResourceState resourceState = new ApplicationDownloadResourceState(url, version, message, patchPercent, ApplicationDownloadState.DOWNLOADING);
        onUpdate(url, resourceState);

        onUpdate(overallPercent);
    }

    /**
     * A JNLP client's DownloadService implementation should call this method if a download fails or aborts unexpectedly.
     * In response, a DownloadServiceListener implementation may display update information to the user to reflect this.
     *
     * @param url     The URL representing the resource for which the download failed.
     * @param version The version of the resource for which the download failed.
     */
    @Override
    public void downloadFailed(final URL url, final String version) {
        setVisible(false);
        dispose();
        final Translator translator = Translator.getInstance();
        ErrorDialog.show(translator.translate("appDownload.error"), new RuntimeException("Error while downloading url '" + url + "' with version '" + version + "'"));
    }
}
