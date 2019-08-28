package com.openwebstart.jvm.ui;

import com.openwebstart.func.Result;
import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.localfinder.JdkFinder;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ConfigurationDialog;
import com.openwebstart.jvm.ui.dialogs.ErrorDialog;
import com.openwebstart.jvm.ui.list.RuntimeListActionSupplier;
import com.openwebstart.jvm.ui.list.RuntimeListComponent;
import com.openwebstart.jvm.ui.list.RuntimeListModel;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

public final class RuntimeManagerPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeManagerPanel.class);

    private final RuntimeListModel listModel;

    private final Executor backgroundExecutor = Executors.newCachedThreadPool();

    public RuntimeManagerPanel() {
        this(JNLPRuntime.getConfiguration());
    }

    public RuntimeManagerPanel(final DeploymentConfiguration deploymentConfiguration) {
        RuntimeManagerConfig.setConfiguration(deploymentConfiguration);
        final RuntimeListActionSupplier supplier = new RuntimeListActionSupplier((oldValue, newValue) -> backgroundExecutor.execute(() -> LocalRuntimeManager.getInstance().replace(oldValue, newValue)));
        final RuntimeListComponent runtimeListComponent = new RuntimeListComponent(supplier);
        listModel = runtimeListComponent.getModel();
        final JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> backgroundExecutor.execute(() -> {
            try {
                LocalRuntimeManager.getInstance().loadRuntimes();
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        }));
        final JButton findLocalRuntimesButton = new JButton("Find local");
        findLocalRuntimesButton.addActionListener(e -> backgroundExecutor.execute(() -> {
            try {
                final List<Result<LocalJavaRuntime>> result = LocalRuntimeManager.getInstance().findAndAddLocalRuntimes();
                result.stream()
                        .filter(r -> !r.isSuccessful())
                        .map(Result::getException)
                        .forEach(ex ->
                                SwingUtilities.invokeLater(() -> new ErrorDialog("Can not add runtime!", ex).showAndWait()));
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        }));

        final JButton configureButton = new JButton("Settings");
        configureButton.addActionListener(e -> new ConfigurationDialog().showAndWait());

        final JButton addLocalRuntimesButton = new JButton("Add local");
        addLocalRuntimesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select JVM");
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileHidingEnabled(false);
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                final Path selected = fileChooser.getSelectedFile().toPath();
                backgroundExecutor.execute(() -> addLocalRuntimes(selected));
            }
        });

        setLayout(new BorderLayout(12, 12));

        add(new JScrollPane(runtimeListComponent), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
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

    private void addLocalRuntimes(final Path selected) {
        try {
            final List<Result<LocalJavaRuntime>> localJdks = JdkFinder.findLocalJdks(selected).stream()
                    .map(this::checkSupportedVersionRange)
                    .collect(Collectors.toList());

            localJdks.stream()
                    .filter(Result::isSuccessful)
                    .map(Result::getResult)
                    .forEach(LocalRuntimeManager.getInstance()::add);

            localJdks.stream()
                    .filter(Result::isFailed)
                    .map(Result::getException)
                    .peek(e1 -> LOG.info("Exception while find local JDKs", e1))
                    .findFirst()
                    .ifPresent(e2 -> SwingUtilities.invokeLater(() -> new ErrorDialog("Error while adding runtime", e2).showAndWait()));

        } catch (final Exception ex) {
            SwingUtilities.invokeLater(() -> new ErrorDialog("Error while adding runtime", ex).showAndWait());
        }
    }

    private Result<LocalJavaRuntime> checkSupportedVersionRange(final Result<LocalJavaRuntime> result) {
        if (result.isSuccessful()) {
            final VersionId version = result.getResult().getVersion();
            if (Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(v -> v.contains(version)).orElse(true)) {
                return result;
            }
            else {
                return Result.fail(new IllegalStateException("Supported version range: " + RuntimeManagerConfig.getSupportedVersionRange()));
            }
        }
        return result;
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
