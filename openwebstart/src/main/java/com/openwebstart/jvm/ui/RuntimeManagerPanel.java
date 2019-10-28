package com.openwebstart.jvm.ui;

import com.openwebstart.func.Result;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.localfinder.JdkFinder;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ConfigurationDialog;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.list.RuntimeListActionSupplier;
import com.openwebstart.jvm.ui.list.RuntimeListComponent;
import com.openwebstart.jvm.ui.list.RuntimeListModel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class RuntimeManagerPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeManagerPanel.class);

    private final RuntimeListModel listModel;

    private final Executor backgroundExecutor = Executors.newCachedThreadPool();

    private final Translator translator;

    public RuntimeManagerPanel() {
        this(JNLPRuntime.getConfiguration());
    }

    public RuntimeManagerPanel(final DeploymentConfiguration deploymentConfiguration) {
        translator = Translator.getInstance();

        RuntimeManagerConfig.setConfiguration(deploymentConfiguration);
        JavaRuntimeManager.reloadLocalRuntimes();
        final RuntimeListActionSupplier supplier = new RuntimeListActionSupplier((oldValue, newValue) -> backgroundExecutor.execute(() -> LocalRuntimeManager.getInstance().replace(oldValue, newValue)));
        final RuntimeListComponent runtimeListComponent = new RuntimeListComponent(supplier);
        listModel = runtimeListComponent.getModel();

        final JButton refreshButton = new JButton(translator.translate("jvmManager.action.refresh.text"));
        refreshButton.addActionListener(e -> backgroundExecutor.execute(() -> {
            try {
                JavaRuntimeManager.reloadLocalRuntimes();
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        }));

        final JButton findLocalRuntimesButton = new JButton(translator.translate("jvmManager.action.findLocal.text"));
        findLocalRuntimesButton.addActionListener(e -> backgroundExecutor.execute(() -> {
            try {
                final List<Result<LocalJavaRuntime>> result = LocalRuntimeManager.getInstance().findAndAddLocalRuntimes();
                result.stream()
                        .filter(r -> !r.isSuccessful())
                        .map(Result::getException)
                        .forEach(ex -> DialogFactory.showErrorDialog(translator.translate("jvmManager.error.addRuntime"), ex));
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        }));

        final JButton configureButton = new JButton(translator.translate("jvmManager.action.settings.text"));
        configureButton.addActionListener(e -> new ConfigurationDialog(deploymentConfiguration).showAndWait());

        final JButton addLocalRuntimesButton = new JButton(translator.translate("jvmManager.action.addLocal.text"));
        addLocalRuntimesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(translator.translate("jvmManager.selectJvm"));
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

        setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

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
                    .peek(e1 -> LOG.info("Exception while finding local JDKs", e1))
                    .findFirst()
                    .ifPresent(e2 -> DialogFactory.showErrorDialog(translator.translate("jvmManager.error.addRuntime"), e2));

        } catch (final Exception ex) {
            DialogFactory.showErrorDialog(translator.translate("jvmManager.error.addRuntime"), ex);
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
