package com.openwebstart.os;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ShortcutCreationOptions;

import java.util.concurrent.Executors;

import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ALWAYS;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ALWAYS_IF_HINTED;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ASK_USER;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ASK_USER_IF_HINTED;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_NEVER;

public class MenuAndDesktopEntryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MenuAndDesktopEntryHandler.class);

    public void askForIntegration(final JNLPFile jnlpFile) {
        MenuAndDesktopEntriesFactory.forCurrentOs().ifPresent(f -> askForIntegration(f, jnlpFile));
    }

    private void askForIntegration(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile) {
        final String property = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT);
        final ShortcutCreationOptions shortcutCreationOptions = ShortcutCreationOptions.forConfigName(property).orElse(CREATE_NEVER);

        if (shortcutCreationOptions == CREATE_NEVER) {
            return;
        }

        final String appName = jnlpFile.createNameForDesktopFile();

        final boolean hasMenu = factory.existsMenuEntry(jnlpFile);
        final boolean hasDesktop = factory.existsDesktopEntry(jnlpFile);

        final boolean supportsMenu = factory.supportsMenuEntry();
        final boolean supportsDesktop = factory.supportsDesktopEntry();

        final boolean jnlpWantsMenu = jnlpFile.getInformation().getShortcut().toMenu();
        final boolean jnlpWantsDesktop = jnlpFile.getInformation().getShortcut().onDesktop();


        if (hasMenu || hasDesktop) {
            updateEntries(factory, jnlpFile, supportsMenu && hasDesktop, supportsDesktop && hasDesktop);
        } else {
            if (shortcutCreationOptions == CREATE_ALWAYS) {
                addEntries(factory, jnlpFile, supportsMenu, supportsDesktop);
            } else if (shortcutCreationOptions == CREATE_ALWAYS_IF_HINTED) {
                addEntries(factory, jnlpFile, supportsMenu && jnlpWantsMenu, supportsDesktop && jnlpWantsDesktop);
            } else if (shortcutCreationOptions == CREATE_ASK_USER) {
                final AskForEntriesDialog dialog = new AskForEntriesDialog(appName, supportsMenu, supportsDesktop);
                final AskForEntriesDialogResult result = dialog.showAndWaitForResult();
                addEntries(factory, jnlpFile, result.isMenuSelected(), result.isDesktopSelected());
            } else if (shortcutCreationOptions == CREATE_ASK_USER_IF_HINTED && (jnlpWantsDesktop || jnlpWantsMenu)) {
                final AskForEntriesDialog dialog = new AskForEntriesDialog(appName, supportsMenu && jnlpWantsMenu, supportsDesktop && jnlpWantsDesktop);
                final AskForEntriesDialogResult result = dialog.showAndWaitForResult();
                addEntries(factory, jnlpFile, result.isMenuSelected(), result.isDesktopSelected());
            }
        }
    }

    private void updateEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile, final boolean updateMenu, final boolean updateDesktop) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (updateMenu) {
                try {
                    factory.updateMenuEntry(jnlpFile);
                } catch (Exception e) {
                    LOG.error("Can not update menu entry for app");
                }
            }

            if (updateDesktop) {
                try {
                    factory.updateDesktopEntry(jnlpFile);
                } catch (Exception e) {
                    LOG.error("Can not update desktop entry for app");
                }
            }
        });
    }

    private void addEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile, final boolean addMenu, final boolean addDesktop) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (addMenu) {
                try {
                    factory.createMenuEntry(jnlpFile);
                } catch (Exception e) {
                    LOG.error("Can not create menu entry for app");
                }
            }
            if (addDesktop) {
                try {
                    factory.createDesktopEntry(jnlpFile);
                } catch (Exception e) {
                    LOG.error("Can not create desktop entry for app");
                }
            }
        });
    }
}
