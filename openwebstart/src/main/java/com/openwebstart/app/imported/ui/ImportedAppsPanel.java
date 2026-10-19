package com.openwebstart.app.imported.ui;

import com.openwebstart.app.imported.DesktopShortcutCreator;
import com.openwebstart.app.imported.ImportedApp;
import com.openwebstart.app.imported.ImportedAppLauncher;
import com.openwebstart.app.imported.ImportedAppStore;
import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.TransferHandler;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Lets the user import .jar / .jnlp files, choose the Java runtime for each of them and create desktop shortcuts.
 */
public class ImportedAppsPanel extends JPanel {

    private final Translator translator = Translator.getInstance();
    private final List<ImportedApp> apps = new ArrayList<>();
    private final AbstractTableModel model = new AbstractTableModel() {
        private final String[] columns = {t("importedApps.column.name"), t("importedApps.column.type"), t("importedApps.column.java"), t("importedApps.column.file")};

        @Override
        public int getRowCount() {
            return apps.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(final int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(final int row, final int column) {
            final ImportedApp a = apps.get(row);
            switch (column) {
                case 0:
                    return a.getName();
                case 1:
                    return a.getType();
                case 2:
                    return a.getJavaLabel() != null ? a.getJavaLabel() : t("importedApps.java.automatic");
                default:
                    return a.getFile().getPath();
            }
        }
    };
    private final JTable table = new JTable(model);

    public ImportedAppsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        final JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        setTransferHandler(dropHandler);
        table.setTransferHandler(dropHandler);
        scrollPane.setTransferHandler(dropHandler);
        add(scrollPane, BorderLayout.CENTER);
        add(new JLabel(t("importedApps.dropHint"), JLabel.CENTER), BorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(button("importedApps.action.add", this::addFiles));
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(button("importedApps.action.rename", this::rename));
        buttons.add(button("importedApps.action.changeJava", this::changeJava));
        buttons.add(button("importedApps.action.run", this::run));
        buttons.add(button("importedApps.action.shortcut", this::createShortcut));
        buttons.add(button("importedApps.action.remove", this::remove));
        add(buttons, BorderLayout.SOUTH);

        reload();
    }

    private String t(final String key) {
        return Translator.getInstance().translate(key);
    }

    private JButton button(final String key, final Runnable action) {
        final JButton b = new JButton(t(key));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void reload() {
        apps.clear();
        apps.addAll(ImportedAppStore.getInstance().getAll());
        model.fireTableDataChanged();
    }

    private Optional<ImportedApp> selected() {
        final int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, t("importedApps.selectFirst"));
            return Optional.empty();
        }
        return Optional.of(apps.get(row));
    }

    private void addFiles() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Java (*.jar, *.jnlp)", "jar", "jnlp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        addAll(Arrays.asList(chooser.getSelectedFiles()));
    }

    private void addAll(final List<File> files) {
        try {
            for (final File f : files) {
                final String n = f.getName().toLowerCase(Locale.ROOT);
                if (f.isFile() && (n.endsWith(".jar") || n.endsWith(".jnlp"))) {
                    ImportedAppStore.getInstance().add(f);
                }
            }
            reload();
        } catch (final Exception e) {
            DialogFactory.showErrorDialog(t("importedApps.error.add"), e);
        }
    }

    /**
     * Accepts .jar / .jnlp files dropped from the file manager.
     */
    private final TransferHandler dropHandler = new TransferHandler() {
        @Override
        public boolean canImport(final TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(final TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                addAll((List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                return true;
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(t("importedApps.error.add"), e);
                return false;
            }
        }
    };

    private void rename() {
        selected().ifPresent(app -> {
            final String input = (String) JOptionPane.showInputDialog(this, t("importedApps.rename.prompt"), t("importedApps.action.rename"),
                    JOptionPane.PLAIN_MESSAGE, null, null, app.getName());
            if (input == null || input.trim().isEmpty() || input.trim().equals(app.getName())) {
                return;
            }
            try {
                ImportedAppStore.getInstance().save(app.withName(input.trim()));
                reload();
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(t("importedApps.error.save"), e);
            }
        });
    }

    private void changeJava() {
        selected().ifPresent(app -> {
            final JavaChooserDialog dialog = new JavaChooserDialog(this, app.getJavaHome());
            if (!dialog.showDialog()) {
                return;
            }
            final LocalJavaRuntime chosen = dialog.getSelectedRuntime();
            final ImportedApp updated = chosen == null
                    ? app.withJava(null, null)
                    : app.withJava(chosen.getJavaHome().toString(), JavaChooserDialog.label(chosen));
            try {
                ImportedAppStore.getInstance().save(updated);
                reload();
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(t("importedApps.error.save"), e);
            }
        });
    }

    private void run() {
        selected().ifPresent(app -> {
            final File log = ImportedAppStore.logFile(app.getId());
            try {
                final Process process = new ProcessBuilder(ImportedAppLauncher.commandFor(app))
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.appendTo(log))
                        .start();
                // an application that ends within a few seconds with an error did not start: tell the user
                getNonDaemonExecutorService().execute(() -> {
                    try {
                        if (process.waitFor(8, TimeUnit.SECONDS) && process.exitValue() != 0) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                    t("importedApps.error.run") + " (exit code " + process.exitValue() + ")\n" + t("importedApps.error.log") + "\n" + log + "\n\n" + tail(log),
                                    t("importedApps.error.run"), JOptionPane.ERROR_MESSAGE));
                        }
                    } catch (final InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(t("importedApps.error.run"), e);
            }
        });
    }

    private static String tail(final File file) {
        try {
            final List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            return String.join("\n", lines.subList(Math.max(0, lines.size() - 8), lines.size()));
        } catch (final Exception e) {
            return "";
        }
    }

    private void createShortcut() {
        selected().ifPresent(app -> {
            try {
                final File shortcut = DesktopShortcutCreator.create(app);
                JOptionPane.showMessageDialog(this, t("importedApps.shortcutCreated") + "\n" + shortcut);
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(t("importedApps.error.shortcut"), e);
            }
        });
    }

    private void remove() {
        selected().ifPresent(app -> {
            try {
                ImportedAppStore.getInstance().remove(app);
                reload();
            } catch (final Exception e) {
                DialogFactory.showErrorDialog(t("importedApps.error.save"), e);
            }
        });
    }
}
