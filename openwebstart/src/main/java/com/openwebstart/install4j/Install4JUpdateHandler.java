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

import java.io.IOException;
import java.util.Optional;

public class Install4JUpdateHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Install4JUpdateHandler.class);

    private final static String UPDATE_PROCESS_ID = "1462";

    public Install4JUpdateHandler(final UpdateSchedule updateSchedule) {
        Assert.requireNonNull(updateSchedule, "updateSchedule");

        UpdateScheduleRegistry.setUpdateSchedule(updateSchedule);
    }

    public void triggerPossibleUpdate() throws UserCanceledException, IOException {
        if (UpdateScheduleRegistry.checkAndReset() && hasUpdate()) {
            doUpdate();
        }
    }

    public static boolean hasUpdate() throws UserCanceledException, IOException {
        final Optional<UpdateDescriptorEntry> possibleUpdateEntry = getUpdate();
        if (possibleUpdateEntry.isPresent()) {
            LOG.info("found update {}", possibleUpdateEntry.get());
        } else {
            LOG.info("No update found on server");
        }
        return possibleUpdateEntry.isPresent();
    }

    public static Optional<UpdateDescriptorEntry> getUpdate() throws UserCanceledException, IOException {
        final String serverUrl = Install4JUtils.updatesUrl();
        LOG.info("Checking for update on server {}", serverUrl);
        final UpdateCheckRequest request = new UpdateCheckRequest(serverUrl);
        request.applicationDisplayMode(ApplicationDisplayMode.UNATTENDED);
        final UpdateDescriptor updateDescriptor = UpdateChecker.getUpdateDescriptor(request);
        return Optional.ofNullable(updateDescriptor.getPossibleUpdateEntry());
    }

    public static void doUpdate() {
        LOG.info("Starting update");
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
