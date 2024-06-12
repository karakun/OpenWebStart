package com.openwebstart.os;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.os.linux.LinuxEntryFactory;
import com.openwebstart.os.mac.MacEntryFactory;
import com.openwebstart.os.win.WinEntryFactory;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import java.util.Optional;

public interface MenuAndDesktopEntriesFactory {
    static final Logger LOG = LoggerFactory.getLogger(MenuAndDesktopEntriesFactory.class);

    void updateDesktopEntry(final JNLPFile file) throws Exception;

    void updateMenuEntry(final JNLPFile file) throws Exception;

    void createDesktopEntry(final JNLPFile file) throws Exception;

    void createMenuEntry(final JNLPFile file) throws Exception;

    boolean existsDesktopEntry(final JNLPFile file);

    boolean existsMenuEntry(final JNLPFile file);

    static Optional<MenuAndDesktopEntriesFactory> forCurrentOs() {
        final OperationSystem localSystem = OperationSystem.getLocalSystem();
        LOG.debug("Choosing MenuAndDesktopEntriesFactory  for OS {}", localSystem.toString());
        if (OperationSystem.MAC64 == localSystem || OperationSystem.MACARM64 == localSystem ) {
            LOG.debug("Choosing MacEntryFactory  for shortcuts");
            return Optional.of(new MacEntryFactory());
        }
        if (OperationSystem.WIN32 == localSystem || OperationSystem.WIN64 == localSystem) {
            LOG.debug("Choosing WinEntryFactory  for shortcuts");
            return Optional.of(new WinEntryFactory());
        }
        if (OperationSystem.LINUX32 == localSystem || OperationSystem.LINUX64 == localSystem) {
            LOG.debug("Using LinuxEntryFactory for shortcuts");
            return Optional.of(new LinuxEntryFactory());
        }
        LOG.debug("Could not find MenuAndDesktopEntriesFactory");
        return Optional.empty();
    }

}
