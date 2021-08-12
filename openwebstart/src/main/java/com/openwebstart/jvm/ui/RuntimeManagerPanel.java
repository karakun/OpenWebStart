package com.openwebstart.jvm.ui;

import com.openwebstart.func.Result;
import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.localfinder.JdkFinder;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.ConfigurationDialog;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.list.RuntimeListActionSupplier;
import com.openwebstart.jvm.ui.list.RuntimeListComponent;
import com.openwebstart.ui.ListComponentModel;
import com.openwebstart.ui.Notifications;
import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;

public final class RuntimeManagerPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeManagerPanel.class);

    private final ListComponentModel<LocalJavaRuntime> listModel;

    private final Translator translator;
    private final DeploymentConfiguration configuration;
    private final LocalRuntimeManager localRuntimeManager;

    public RuntimeManagerPanel(final DeploymentConfiguration deploymentConfiguration) {
        translator = Translator.getInstance();
        configuration = deploymentConfiguration;
        localRuntimeManager = LocalRuntimeManager.getInstance();

        RuntimeManagerConfig.setConfiguration(deploymentConfiguration);
        JavaRuntimeManager.reloadLocalRuntimes(configuration);
        final RuntimeListActionSupplier supplier = new RuntimeListActionSupplier((oldValue, newValue) -> getNonDaemonExecutorService().execute(() -> localRuntimeManager.replace(oldValue, newValue)));
        final RuntimeListComponent runtimeListComponent = new RuntimeListComponent(supplier);
        listModel = runtimeListComponent.getModel();

        final JButton removeAllRuntimes = new JButton(translator.translate("jvmManager.action.removeAll.text"));
        removeAllRuntimes.addActionListener(e -> onRemoveAll());

        final JButton refreshButton = new JButton(translator.translate("jvmManager.action.refresh.text"));
        refreshButton.addActionListener(e -> onRefresh());

        final JButton findLocalRuntimesButton = new JButton(translator.translate("jvmManager.action.findLocal.text"));
        findLocalRuntimesButton.addActionListener(e -> onFindLocalRuntimes(deploymentConfiguration));

        final JButton configureButton = new JButton(translator.translate("jvmManager.action.settings.text"));
        configureButton.addActionListener(e -> new ConfigurationDialog(deploymentConfiguration).showAndWait());

        final JButton addLocalRuntimesButton = new JButton(translator.translate("jvmManager.action.addLocal.text"));
        addLocalRuntimesButton.addActionListener(e -> onAddLocalRuntime());

        setLayout(LayoutFactory.createBorderLayout(12, 12));

        add(new JScrollPane(runtimeListComponent), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(LayoutFactory.createBoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(removeAllRuntimes);
        buttonPanel.add(refreshButton);
        buttonPanel.add(addLocalRuntimesButton);
        buttonPanel.add(findLocalRuntimesButton);
        buttonPanel.add(configureButton);


        add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        //TODO: Register on show and hide on close
        localRuntimeManager.addRuntimeAddedListener(runtime -> SwingUtilities.invokeLater(() -> listModel.addElement(runtime)));
        localRuntimeManager.addRuntimeRemovedListener(runtime -> SwingUtilities.invokeLater(() -> listModel.removeElement(runtime)));
        localRuntimeManager.addRuntimeUpdatedListener((oldValue, newValue) -> SwingUtilities.invokeLater(() -> listModel.replaceItem(oldValue, newValue)));

        listModel.replaceData(localRuntimeManager.getAll());
    }

    private void onRemoveAll() {
        final List<LocalJavaRuntime> runtimes = Collections.list(listModel.elements());

        getNonDaemonExecutorService().execute(() -> {
            try {
                localRuntimeManager.removeAll(runtimes);
                SwingUtilities.invokeLater(() -> listModel.removeAllElements());
            } catch (final Exception e) {
                onRefresh();
                DialogFactory.showErrorDialog(Translator.getInstance().translate("jvmManager.error.deleteFolder"), e);
            }
        });
    }

    private void onRefresh() {
        getNonDaemonExecutorService().execute(() -> {
            try {
                JavaRuntimeManager.reloadLocalRuntimes(configuration);
            } catch (Exception ex) {
                throw new RuntimeException("Error", ex);
            }
        });
    }

    private void onAddLocalRuntime() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(translator.translate("jvmManager.selectJvm"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final Path selected = fileChooser.getSelectedFile().toPath();
            getNonDaemonExecutorService().execute(() -> {
                try {
                    handleFoundRuntimes(JdkFinder.findLocalRuntimes(selected));
                } catch (final Exception ex) {
                    DialogFactory.showErrorDialog(translator.translate("jvmManager.error.addRuntime"), ex);
                }
            });
        }
    }

    private void handleFoundRuntimes(final List<ResultWithInput<Path, LocalJavaRuntime>> results) {
        Assert.requireNonNull(results, "results");
        final List<LocalJavaRuntime> successful = extractSuccessfulFoundRuntimes(results);
        final long added = localRuntimeManager.addNewLocalJavaRuntime(successful, Notifications::showError);
        if (added > 0) {
            LOG.info("Added {} local JVMs to the JVM Manager", added);
            Notifications.showInfo(Translator.getInstance().translate("jvmManager.info.jvmsAdded", added));
        } else {
            LOG.info("No local JVMs added to the JVM Manager", added);
            Notifications.showInfo(Translator.getInstance().translate("jvmManager.info.noJvmAdded"));
        }
    }

    private List<LocalJavaRuntime> extractSuccessfulFoundRuntimes(List<ResultWithInput<Path, LocalJavaRuntime>> results) {
        final List<LocalJavaRuntime> successful = results.stream()
                .map(result -> {
                    if (result.isSuccessful() && result.getResult() == null) {
                        return Result.<Path, LocalJavaRuntime>fail(result.getInput(), new NullPointerException("runtime == null"));
                    }
                    return result;
                })
                .filter(result -> {
                    if (result.isFailed()) {
                        LOG.error("Error while adding local JDK at '" + result.getInput() + "'", result.getException());
                        Notifications.showError(Translator.getInstance().translate("jvmManager.error.jvmNotAdded"));
                        return false;
                    }
                    return true;
                })
                .map(Result::getResult)
                .collect(Collectors.toList());
        return successful;
    }

    private void onFindLocalRuntimes(final DeploymentConfiguration deploymentConfiguration) {
        LOG.info("Starting to search for local JVMs");
        getNonDaemonExecutorService().execute(() -> {
            try {
                handleFoundRuntimes(JdkFinder.findLocalRuntimes(deploymentConfiguration));
            } catch (final Exception ex) {
                DialogFactory.showErrorDialog(translator.translate("jvmManager.error.addRuntimes"), ex);
            }
        });
    }

}
