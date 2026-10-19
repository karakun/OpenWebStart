package com.openwebstart.app.imported.ui;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.JavaDownloadPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Dialog to pick the Java runtime for an application: either one that is already installed
 * or one that is searched on the JVM server and downloaded on the spot.
 */
class JavaChooserDialog extends JDialog {

    private final List<LocalJavaRuntime> installed = new ArrayList<>();
    private final JComboBox<String> installedCombo = new JComboBox<>();

    private boolean confirmed;

    JavaChooserDialog(final Component parent, final String currentJavaHome) {
        super(javax.swing.SwingUtilities.getWindowAncestor(parent), t("importedApps.action.changeJava"), ModalityType.APPLICATION_MODAL);

        reloadInstalled(currentJavaHome);

        final JPanel installedPanel = new JPanel(new BorderLayout(8, 8));
        installedPanel.setBorder(BorderFactory.createTitledBorder(t("importedApps.chooser.installed")));
        installedPanel.add(installedCombo, BorderLayout.CENTER);

        final JavaDownloadPanel downloadPanel = new JavaDownloadPanel(done -> reloadInstalled(done.getJavaHome().toString()));

        final JButton ok = new JButton(t("importedApps.chooser.ok"));
        final JButton cancel = new JButton(t("importedApps.chooser.cancel"));
        ok.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(ok);

        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(installedPanel);
        content.add(Box.createVerticalStrut(8));
        content.add(downloadPanel);
        content.add(buttons);
        setContentPane(content);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(parent);
    }

    private static String t(final String key) {
        return Translator.getInstance().translate(key);
    }

    private void reloadInstalled(final String selectJavaHome) {
        installed.clear();
        installed.addAll(LocalRuntimeManager.getInstance().getAll().stream().filter(LocalJavaRuntime::isActive).collect(Collectors.toList()));
        installedCombo.removeAllItems();
        installedCombo.addItem(t("importedApps.java.automatic"));
        int selected = 0;
        for (int i = 0; i < installed.size(); i++) {
            installedCombo.addItem(label(installed.get(i)));
            if (installed.get(i).getJavaHome().toString().equals(selectJavaHome)) {
                selected = i + 1;
            }
        }
        installedCombo.setSelectedIndex(selected);
    }

    static String label(final LocalJavaRuntime r) {
        return r.getVersion() + " (" + r.getVendor() + ")";
    }

    /**
     * Shows the dialog.
     *
     * @return false if the user cancelled
     */
    boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    /**
     * @return the chosen runtime or null for the automatic selection
     */
    LocalJavaRuntime getSelectedRuntime() {
        final int idx = installedCombo.getSelectedIndex();
        return idx <= 0 ? null : installed.get(idx - 1);
    }
}
