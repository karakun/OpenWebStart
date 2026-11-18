package com.openwebstart.jvm.ui;

import com.openwebstart.jvm.RemoteRuntimeSearch;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;

/**
 * Search the Java versions offered by the JVM server (e.g. "1.8", "17") and download one of them.
 */
public class JavaDownloadPanel extends JPanel {

    private final JTextField searchField = new JTextField(12);
    private final JButton searchButton = new JButton();
    private final JButton downloadButton = new JButton();
    private final DefaultListModel<RemoteJavaRuntime> resultModel = new DefaultListModel<>();
    private final JList<RemoteJavaRuntime> resultList = new JList<>(resultModel);
    private final JLabel status = new JLabel(" ");
    private final Consumer<LocalJavaRuntime> onInstalled;

    /**
     * @param onInstalled called on the UI thread after a runtime was downloaded and installed
     */
    public JavaDownloadPanel(final Consumer<LocalJavaRuntime> onInstalled) {
        super(new BorderLayout(8, 8));
        this.onInstalled = onInstalled;

        setBorder(BorderFactory.createTitledBorder(t("importedApps.chooser.available")));
        searchButton.setText(t("importedApps.chooser.search"));
        downloadButton.setText(t("importedApps.chooser.download"));
        downloadButton.setEnabled(false);
        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        downloadButton.addActionListener(e -> download());

        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setCellRenderer((list, value, index, isSelected, hasFocus) -> {
            final JLabel l = new JLabel(" " + value.getVersion() + "  (" + value.getVendor() + ", " + value.getOperationSystem().getDescription() + ")");
            l.setOpaque(true);
            l.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            l.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return l;
        });
        resultList.addListSelectionListener(e -> downloadButton.setEnabled(resultList.getSelectedValue() != null));

        final JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.add(new JLabel(t("importedApps.chooser.searchHint")));
        searchRow.add(searchField);
        searchRow.add(searchButton);
        add(searchRow, BorderLayout.NORTH);

        final JScrollPane scroll = new JScrollPane(resultList);
        scroll.setPreferredSize(new Dimension(460, 150));
        add(scroll, BorderLayout.CENTER);

        final JPanel south = new JPanel(new BorderLayout(8, 0));
        south.add(status, BorderLayout.CENTER);
        south.add(downloadButton, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);
    }

    private static String t(final String key) {
        return Translator.getInstance().translate(key);
    }

    private void search() {
        final String query = searchField.getText();
        searchButton.setEnabled(false);
        status.setText(t("importedApps.chooser.searching"));
        resultModel.clear();
        getNonDaemonExecutorService().execute(() -> {
            List<RemoteJavaRuntime> found = new ArrayList<>();
            String message;
            try {
                found = RemoteRuntimeSearch.search(query);
                message = found.isEmpty() ? t("importedApps.chooser.nothingFound") : " ";
            } catch (final Exception e) {
                message = t("importedApps.chooser.searchFailed") + " " + e.getMessage();
            }
            final List<RemoteJavaRuntime> result = found;
            final String finalMessage = message;
            SwingUtilities.invokeLater(() -> {
                result.forEach(resultModel::addElement);
                status.setText(finalMessage);
                searchButton.setEnabled(true);
            });
        });
    }

    private void download() {
        final RemoteJavaRuntime runtime = resultList.getSelectedValue();
        if (runtime == null) {
            return;
        }
        downloadButton.setEnabled(false);
        searchButton.setEnabled(false);
        status.setText(t("importedApps.chooser.downloading") + " " + runtime.getVersion() + " ...");
        getNonDaemonExecutorService().execute(() -> {
            String message;
            LocalJavaRuntime installedRuntime = null;
            try {
                installedRuntime = RemoteRuntimeSearch.install(runtime);
                message = t("importedApps.chooser.downloaded");
            } catch (final Exception e) {
                message = t("importedApps.chooser.downloadFailed") + " " + e.getMessage();
            }
            final LocalJavaRuntime done = installedRuntime;
            final String finalMessage = message;
            SwingUtilities.invokeLater(() -> {
                status.setText(finalMessage);
                searchButton.setEnabled(true);
                if (done != null) {
                    resultModel.removeElement(runtime);
                    onInstalled.accept(done);
                } else {
                    downloadButton.setEnabled(resultList.getSelectedValue() != null);
                }
            });
        });
    }
}
