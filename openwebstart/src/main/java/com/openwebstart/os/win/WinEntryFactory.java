package com.openwebstart.os.win;

import com.openwebstart.os.MenuAndDesktopEntriesFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.util.WindowsDesktopEntry;

import java.io.IOException;

public class WinEntryFactory implements MenuAndDesktopEntriesFactory {

    @Override
    public void updateDesktopEntry(final JNLPFile file) throws IOException {
        createDesktopEntry(file);
    }

    @Override
    public void updateMenuEntry(final JNLPFile file) throws IOException {
        createMenuEntry(file);
    }

    @Override
    public void createDesktopEntry(final JNLPFile file) throws IOException {
        final WindowsDesktopEntry desktopEntry = new WindowsDesktopEntry(file);
        desktopEntry.createShortcutOnWindowsDesktop();
    }

    @Override
    public void createMenuEntry(final JNLPFile file) throws IOException {
        final WindowsDesktopEntry desktopEntry = new WindowsDesktopEntry(file);
        desktopEntry.createWindowsMenu();
    }

    @Override
    public boolean existsDesktopEntry(final JNLPFile file) {
        final WindowsDesktopEntry desktopEntry = new WindowsDesktopEntry(file);
        return desktopEntry.getDesktopIconFile().exists();
    }

    @Override
    public boolean existsMenuEntry(final JNLPFile file) {
        return false;
    }
}
