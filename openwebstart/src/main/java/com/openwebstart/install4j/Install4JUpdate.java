package com.openwebstart.install4j;

import com.install4j.api.context.UserCanceledException;
import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.update.ApplicationDisplayMode;
import com.install4j.api.update.UpdateCheckRequest;
import com.install4j.api.update.UpdateChecker;
import com.install4j.api.update.UpdateDescriptor;
import com.install4j.api.update.UpdateDescriptorEntry;
import com.install4j.api.update.UpdateSchedule;
import com.install4j.api.update.UpdateScheduleRegistry;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.io.IOException;
import java.util.Optional;

public class Install4JUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(Install4JUpdate.class);

    private final static String UPDATE_PROCESS_ID = "1462";


    private final static String UPDATE_DEACTIVATED_PARAM = "ows.update.deactivated";

    private final static String UPDATE_SCHEDULE_PARAM = "ows.update.schedule";

    private final static String UPDATE_SERVER_PARAM = "ows.update.server";

    private final static String DEFAULT_SERVER_URL = "http://localhost:8080/updates.xml";

    private final boolean updateDeactivated;

    private final String updateServerUrl;



    public Install4JUpdate(final DeploymentConfiguration configuration) {
        Assert.requireNonNull(configuration, "configuration");

        final UpdateSchedule schedule = Optional.ofNullable(UpdateSchedule.getById(configuration.getProperty(UPDATE_SCHEDULE_PARAM)))
                .orElse(UpdateSchedule.ON_EVERY_START);
        UpdateScheduleRegistry.setUpdateSchedule(schedule);

        updateDeactivated = Boolean.parseBoolean(configuration.getProperty(UPDATE_DEACTIVATED_PARAM));

        updateServerUrl = Optional.ofNullable(configuration.getProperty(UPDATE_SERVER_PARAM)).orElse(DEFAULT_SERVER_URL);
    }

    public boolean isUpdateDeactivated() {
        return updateDeactivated;
    }

    public void triggerPossibleUpdate() throws UserCanceledException, IOException {
        if (!updateDeactivated && UpdateScheduleRegistry.checkAndReset() && hasUpdate()) {
            doUpdate();
        }
    }

    public boolean hasUpdate() throws UserCanceledException, IOException {
        LOG.info("Checking for update on server");
        final UpdateCheckRequest request = new UpdateCheckRequest(updateServerUrl);
        request.applicationDisplayMode(ApplicationDisplayMode.UNATTENDED);
        final UpdateDescriptor updateDescriptor = UpdateChecker.getUpdateDescriptor(request);
        final Optional<UpdateDescriptorEntry> possibleUpdateEntry = Optional.ofNullable(updateDescriptor.getPossibleUpdateEntry());
        if (possibleUpdateEntry.isPresent()) {
            LOG.info("found update {}", possibleUpdateEntry.get());
        } else {
            LOG.info("No update found on server");
        }
        return possibleUpdateEntry.isPresent();
    }

    private void doUpdate() {
        LOG.info("Starting update");
        // This will return immediately if you call it from the EDT,
        // otherwise it will block until the installer application exits

       // PARAM NAME FOR URL:  "installer:updatesUrl"

        ApplicationLauncher.launchApplicationInProcess(UPDATE_PROCESS_ID, null, new ApplicationLauncher.Callback() {
                    public void exited(int exitValue) {
                        LOG.info("Installer closed");
                    }

                    public void prepareShutdown() {
                        LOG.info("Will shut down for update");
                    }
                }, ApplicationLauncher.WindowMode.FRAME, null
        );


    }

}
