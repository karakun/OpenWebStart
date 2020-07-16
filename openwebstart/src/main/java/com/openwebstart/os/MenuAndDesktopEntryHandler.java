package com.openwebstart.os;

import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.MenuAndDesktopIntegration;
import net.sourceforge.jnlp.util.ShortcutCreationOptions;

import java.util.Optional;

import static com.openwebstart.concurrent.ThreadPoolHolder.getNonDaemonExecutorService;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ALWAYS;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ALWAYS_IF_HINTED;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ASK_USER;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_ASK_USER_IF_HINTED;
import static net.sourceforge.jnlp.util.ShortcutCreationOptions.CREATE_NEVER;

public class MenuAndDesktopEntryHandler implements MenuAndDesktopIntegration {

    @Override
    public void addMenuAndDesktopEntries(final JNLPFile jnlpFile) {
        MenuAndDesktopEntriesFactory.forCurrentOs().ifPresent(f -> addMenuAndDesktopEntries(f, jnlpFile));
    }

    private void addMenuAndDesktopEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile) {
        final String property = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT);
        final ShortcutCreationOptions shortcutCreationOptions = ShortcutCreationOptions.forConfigName(property).orElse(CREATE_NEVER);

        if (shortcutCreationOptions == CREATE_NEVER) {
            return;
        }

        final String appName = jnlpFile.getShortcutName();

        final boolean hasMenu = factory.existsMenuEntry(jnlpFile);
        final boolean hasDesktop = factory.existsDesktopEntry(jnlpFile);

        final boolean supportsMenu = factory.supportsMenuEntry();
        final boolean supportsDesktop = factory.supportsDesktopEntry();

        final Optional<ShortcutDesc> shortcutDesc = Optional.ofNullable(jnlpFile.getInformation()).map(InformationDesc::getShortcut);

        final boolean jnlpWantsMenu = shortcutDesc.map(ShortcutDesc::toMenu).orElse(false);
        final boolean jnlpWantsDesktop = shortcutDesc.map(ShortcutDesc::onDesktop).orElse(false);


        if (hasMenu || hasDesktop) {
            updateEntries(factory, jnlpFile, supportsMenu && hasMenu, supportsDesktop && hasDesktop);
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
        getNonDaemonExecutorService().execute(() -> {
            if (updateMenu) {
                try {
                    factory.updateMenuEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not update menu entry for app", e);
                }
            }

            if (updateDesktop) {
                try {
                    factory.updateDesktopEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not update desktop entry for app", e);
                }
            }
        });
    }

    private void addEntries(final MenuAndDesktopEntriesFactory factory, final JNLPFile jnlpFile, final boolean addMenu, final boolean addDesktop) {
        getNonDaemonExecutorService().execute(() -> {
            if (addMenu) {
                try {
                    factory.createMenuEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not create menu entry for app", e);
                }
            }
            if (addDesktop) {
                try {
                    factory.createDesktopEntry(jnlpFile);
                } catch (Exception e) {
                    DialogFactory.showErrorDialog("Can not create desktop entry for app", e);
                }
            }
        });
    }
}
