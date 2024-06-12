package com.openwebstart.os;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.MenuAndDesktopIntegration;
import net.sourceforge.jnlp.util.ShortcutCreationOptions;

import java.util.Optional;

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ALWAYS;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ALWAYS_IF_HINTED;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ASK_USER;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ASK_USER_IF_HINTED;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_NEVER;

public class MenuAndDesktopEntryHandler implements MenuAndDesktopIntegration {
    static final Logger LOG = LoggerFactory.getLogger(MenuAndDesktopEntryHandler.class);

    @Override
    public void addMenuAndDesktopEntries(final JNLPFile jnlpFile) {
        MenuAndDesktopEntriesFactory.forCurrentOs().ifPresent(f -> addMenuAndDesktopEntries(f, jnlpFile));
    }

    private void addMenuAndDesktopEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile) {
        LOG.debug("AddMenuAndDesktopEntries using factory {} and Jnlp file {}", factory != null ? factory.getClass().getName() : "null", jnlpFile.getSourceLocation().toString());
        final String property = JNLPRuntime.getConfiguration().getProperty(KEY_CREATE_DESKTOP_SHORTCUT);
        final ShortcutCreationOptions shortcutCreationOptions = ShortcutCreationOptions.forConfigName(property).orElse(CREATE_NEVER);

        if (shortcutCreationOptions == CREATE_NEVER) {
            LOG.debug("Shortcut creation option {}", shortcutCreationOptions.name());
            return;
        }

        final String appName = jnlpFile.getShortcutName();

        final boolean hasMenu = factory.existsMenuEntry(jnlpFile);
        final boolean hasDesktop = factory.existsDesktopEntry(jnlpFile);

        LOG.debug("Application {} hasMenu {} hasDesktop {}", appName, hasMenu, hasDesktop);

        final Optional<ShortcutDesc> shortcutDesc = Optional.ofNullable(jnlpFile.getInformation()).map(InformationDesc::getShortcut);

        final boolean jnlpWantsMenu = shortcutDesc.map(ShortcutDesc::toMenu).orElse(false);
        final boolean jnlpWantsDesktop = shortcutDesc.map(ShortcutDesc::onDesktop).orElse(false);

        LOG.debug("Jnlp file {} wantsMenu {} wantsDesktop {}", jnlpFile.getSourceLocation().toString(), jnlpWantsMenu, jnlpWantsDesktop);

        if (hasMenu || hasDesktop) {
            ShortcutUpdateStrategy strategy = ShortcutUpdateStrategy.get(JNLPRuntime.getConfiguration().getProperty(OwsDefaultsProvider.SHORTCUT_UPDATE_STRATEGY));
            if (strategy == ShortcutUpdateStrategy.OVERWRITE) {
                updateEntries(factory, jnlpFile, hasMenu, hasDesktop);
            }
        } else {
            if (shortcutCreationOptions == CREATE_ALWAYS) {
                addEntries(factory, jnlpFile, true, true);
            } else if (shortcutCreationOptions == CREATE_ALWAYS_IF_HINTED) {
                addEntries(factory, jnlpFile, jnlpWantsMenu, jnlpWantsDesktop);
            } else if (shortcutCreationOptions == CREATE_ASK_USER) {
                final AskForEntriesDialog dialog = new AskForEntriesDialog(appName, true, true);
                final AskForEntriesDialogResult result = dialog.showAndWaitForResult();
                addEntries(factory, jnlpFile, result.isMenuSelected(), result.isDesktopSelected());
            } else if (shortcutCreationOptions == CREATE_ASK_USER_IF_HINTED && (jnlpWantsDesktop || jnlpWantsMenu)) {
                final AskForEntriesDialog dialog = new AskForEntriesDialog(appName, jnlpWantsMenu, jnlpWantsDesktop);
                final AskForEntriesDialogResult result = dialog.showAndWaitForResult();
                addEntries(factory, jnlpFile, result.isMenuSelected(), result.isDesktopSelected());
            }
        }
    }

    private void updateEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile, final boolean updateMenu, final boolean updateDesktop) {
        getNonDaemonExecutorService().execute(() -> {
            LOG.debug("Updating Menu and Desktop Entries");
            if (updateMenu) {
                try {
                    LOG.debug("Update menu entry for Jnlp file {}", jnlpFile.getSourceLocation().toString());
                    factory.updateMenuEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not update menu entry for app", e);
                }
            }

            if (updateDesktop) {
                try {
                    LOG.debug("Update desktop entry for Jnlp file {}", jnlpFile.getSourceLocation().toString());
                    factory.updateDesktopEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not update desktop entry for app", e);
                }
            }
        });
    }

    private void addEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile, final boolean addMenu, final boolean addDesktop) {
        getNonDaemonExecutorService().execute(() -> {
            LOG.debug("Adding Menu and Desktop Entries");
            if (addMenu) {
                try {
                    LOG.debug("Create menu entry for Jnlp file {}", jnlpFile.getSourceLocation().toString());
                    factory.createMenuEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not create menu entry for app", e);
                }
            }
            if (addDesktop) {
                try {
                    LOG.debug("Create desktop entry for Jnlp file {}", jnlpFile.getSourceLocation().toString());
                    factory.createDesktopEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not create desktop entry for app", e);
                }
            }
        });
    }
}
