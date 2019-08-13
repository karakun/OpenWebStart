package com.openwebstart.jvm.ui;

import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.func.Result;
import com.openwebstart.jvm.localfinder.RuntimeFinder;
import com.openwebstart.jvm.localfinder.RuntimeFinderUtils;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ConfigurationDialog;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class RuntimeManagerPanel extends JPanel {

    private final RuntimeListModel listModel;

    private final Executor backgroundExecutor = Executors.newCachedThreadPool();

    public RuntimeManagerPanel() {
        final RuntimeListActionSupplier supplier = new RuntimeListActionSupplier((oldValue, newValue) -> backgroundExecutor.execute(() -> LocalRuntimeManager.getInstance().replace(oldValue, newValue)));
        RuntimeListComponent runtimeListComponent = new RuntimeListComponent(supplier);
        listModel = new RuntimeListModel();
        runtimeListComponent.setModel(listModel);

        final JButton refreshButton = new JButton("refresh");
        refreshButton.addActionListener(e -> backgroundExecutor.execute(() -> {
            try {
                LocalRuntimeManager.getInstance().loadRuntimes();
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        }));

        final JButton findLocalRuntimesButton = new JButton("find local");
        findLocalRuntimesButton.addActionListener(e -> backgroundExecutor.execute(() -> {
            try {
                final List<Result<LocalJavaRuntime>> result = LocalRuntimeManager.getInstance().findAndAddLocalRuntimes();
                result.stream().filter(r -> !r.isSuccessful()).map(r -> r.getException()).forEach(ex -> SwingUtilities.invokeLater(() -> new ErrorDialog("Can not add runtime!", ex).showAndWait()));
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        }));

        final JButton configureButton = new JButton("configurae");
        configureButton.addActionListener(e -> new ConfigurationDialog().showAndWait());

        final JButton addLocalRuntimesButton = new JButton("add local");
        addLocalRuntimesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select JVM");
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                final Path selected = fileChooser.getSelectedFile().toPath();
                backgroundExecutor.execute(() -> {
                    try {
                        final String version = RuntimeFinderUtils.readVersion(selected);
                        if(Optional.ofNullable(RuntimeManagerConfig.getInstance().getSupportedVersionRange()).map(v -> v.contains(version)).orElse(true)) {
                            final String vendor = RuntimeFinderUtils.readVendor(selected);
                            final LocalJavaRuntime runtime = new LocalJavaRuntime(version, OperationSystem.getLocalSystem(), vendor, selected, LocalDateTime.now(), false, false);
                            LocalRuntimeManager.getInstance().add(runtime);
                        } else {
                            SwingUtilities.invokeLater(() -> new ErrorDialog("Version '" + version + "' of runtime not supported", new IllegalStateException("Supported version range: " + RuntimeManagerConfig.getInstance().getSupportedVersionRange())).showAndWait());
                        }
                    } catch (final Exception ex) {
                        SwingUtilities.invokeLater(() -> new ErrorDialog("Error while adding runtime", ex).showAndWait());
                    }
                });
            }
        });


        setLayout(new BorderLayout());

        add(new JScrollPane(runtimeListComponent), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new

                FlowLayout(FlowLayout.RIGHT, 4, 4));
        buttonPanel.add(refreshButton);
        buttonPanel.add(addLocalRuntimesButton);
        buttonPanel.add(findLocalRuntimesButton);
        buttonPanel.add(configureButton);


        add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        //TODO: Register on show and hide on close
        LocalRuntimeManager.getInstance().

                addRuntimeAddedListener(runtime -> SwingUtilities.invokeLater(() -> listModel.addElement(runtime)));
        LocalRuntimeManager.getInstance().

                addRuntimeRemovedListener(runtime -> SwingUtilities.invokeLater(() -> listModel.removeElement(runtime)));
        LocalRuntimeManager.getInstance().

                addRuntimeUpdatedListener((oldValue, newValue) -> SwingUtilities.invokeLater(() -> listModel.replaceItem(oldValue, newValue)));

        refreshModel();

    }

    private void refreshModel() {
        listModel.clear();
        backgroundExecutor.execute(() -> {
            try {
                final List<LocalJavaRuntime> loadedData = LocalRuntimeManager.getInstance().getAll();
                SwingUtilities.invokeAndWait(() -> listModel.replaceData(loadedData));
            } catch (final Exception e) {
                //TODO: Handle in UI error dialog.
                throw new RuntimeException("Error while loading data", e);
            }
        });
    }
}
