package com.openwebstart.app.ui;

import com.openwebstart.app.Application;
import com.openwebstart.app.ApplicationManager;
import com.openwebstart.app.ui.actions.CreateShortcutAction;
import com.openwebstart.app.ui.actions.DeleteApplicationAction;
import com.openwebstart.app.ui.actions.StartApplicationAction;
import com.openwebstart.func.Result;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.ui.Action;
import com.openwebstart.ui.ListComponentModel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplicationManagerPanel extends JPanel {

    private final static Logger LOG = LoggerFactory.getLogger(ApplicationManagerPanel.class);

    private final Executor backgroundExecutor = Executors.newCachedThreadPool();

    private final Translator translator;

    private final ListComponentModel<Application> listModel;

    public ApplicationManagerPanel(final DeploymentConfiguration deploymentConfiguration) {
        translator = Translator.getInstance();

        final Function<Application, List<Action<Application>>> actionSupplier = a -> {
            final List<Action<Application>> actions = new ArrayList<>();
            actions.add(new StartApplicationAction());
            actions.add(new CreateShortcutAction());
            actions.add(new DeleteApplicationAction(app -> refreshModel()));
            return actions;
        };

        final ApplicationListComponent appListComponent = new ApplicationListComponent(actionSupplier);
        listModel = appListComponent.getModel();


        final JButton refreshButton = new JButton(translator.translate("appManager.action.refresh.text"));
        refreshButton.addActionListener(e -> backgroundExecutor.execute(() -> refreshModel()));

        setLayout(new BorderLayout(12, 12));

        add(new JScrollPane(appListComponent), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(refreshButton);


        add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        refreshModel();
    }

    private void refreshModel() {
        listModel.clear();
        backgroundExecutor.execute(() -> {
            try {
                final List<Result<Application>> loadedData = ApplicationManager.getInstance().getAllApplications();

                final List<Application> apps = loadedData.stream()
                        .filter(Result::isSuccessful)
                        .map(Result::getResult)
                        .collect(Collectors.toList());
                SwingUtilities.invokeAndWait(() -> listModel.replaceData(apps));

                loadedData.stream().filter(Result::isFailed).findFirst().ifPresent(r -> LOG.error("Error while updating model", r.getException()));

            } catch (final Exception e) {
                //TODO: Handle in UI error dialog.
                DialogFactory.showErrorDialog("Can not update model", e);
            }
        });
    }
}
