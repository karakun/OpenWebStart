package com.openwebstart.jvm.ui;

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

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;

public final class RuntimeManagerPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeManagerPanel.class);

    private final ListComponentModel<LocalJavaRuntime> listModel;

    private final Translator translator;
    private final DeploymentConfiguration configuration;

    public RuntimeManagerPanel(final DeploymentConfiguration deploymentConfiguration) {
        translator = Translator.getInstance();
        configuration = deploymentConfiguration;

        RuntimeManagerConfig.setConfiguration(deploymentConfiguration);
        JavaRuntimeManager.reloadLocalRuntimes(configuration);
        final RuntimeListActionSupplier supplier = new RuntimeListActionSupplier((oldValue, newValue) -> getNonDaemonExecutorService().execute(() -> LocalRuntimeManager.getInstance().replace(oldValue, newValue)));
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
        LocalRuntimeManager.getInstance().
                addRuntimeAddedListener(runtime -> SwingUtilities.invokeLater(() -> listModel.addElement(runtime)));

        LocalRuntimeManager.getInstance().
                addRuntimeRemovedListener(runtime -> SwingUtilities.invokeLater(() -> listModel.removeElement(runtime)));

        LocalRuntimeManager.getInstance().
                addRuntimeUpdatedListener((oldValue, newValue) -> SwingUtilities.invokeLater(() -> listModel.replaceItem(oldValue, newValue)));

        listModel.replaceData(LocalRuntimeManager.getInstance().getAll());
    }

    private void onRemoveAll() {

        Collections.list(listModel.elements()).forEach(jvm -> {
            getNonDaemonExecutorService().execute(() -> {
                try {
                    if (jvm.isManaged()) {
                        LocalRuntimeManager.getInstance().delete(jvm);
                    } else {
                        LocalRuntimeManager.getInstance().remove(jvm);
                    }
                } catch (final Exception e) {
                    DialogFactory.showErrorDialog(Translator.getInstance().translate("jvmManager.error.deleteFolder"), e);
                }
            });
        });

        listModel.removeAllElements();
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
        final long added = results.stream()
                .filter(result -> {
                    if (result.isSuccessful()) {
                        return handleSuccessfulFoundRuntime(result.getInput(), result.getResult());
                    } else {
                        LOG.error("Error while adding local JDK at '" + result.getInput() + "'", result.getException());
                        Notifications.showError(Translator.getInstance().translate("jvmManager.error.jvmNotAdded"));
                        return false;
                    }
                }).count();
        if (added > 0) {
            LOG.info("Added {} local JVMs to the JVM Manager", added);
            Notifications.showInfo(Translator.getInstance().translate("jvmManager.info.jvmsAdded", added));
        } else {
            LOG.info("No local JVMs added to the JVM Manager", added);
            Notifications.showInfo(Translator.getInstance().translate("jvmManager.info.noJvmAdded"));
        }
    }

    private boolean handleSuccessfulFoundRuntime(final Path path, final LocalJavaRuntime runtime) {
        if (runtime == null) {
            LOG.error("Error while adding local JDK at '" + path + "'", new NullPointerException("runtime == null"));
            Notifications.showError(Translator.getInstance().translate("jvmManager.error.jvmNotAdded"));
            return false;
        }
        return LocalRuntimeManager.getInstance().addNewLocalJavaRuntime(runtime, Notifications::showError);
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
