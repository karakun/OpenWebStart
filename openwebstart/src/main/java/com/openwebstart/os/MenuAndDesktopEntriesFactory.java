package com.openwebstart.os;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.os.linux.LinuxEntryFactory;
import com.openwebstart.os.mac.MacEntryFactory;
import com.openwebstart.os.win.WinEntryFactory;
import net.sourceforge.jnlp.JNLPFile;

import java.util.Objects;
import java.util.Optional;

public interface MenuAndDesktopEntriesFactory {

    void updateDesktopEntry(final JNLPFile file) throws Exception;

    void updateMenuEntry(final JNLPFile file) throws Exception;

    void createDesktopEntry(final JNLPFile file) throws Exception;

    void createMenuEntry(final JNLPFile file) throws Exception;

    boolean existsDesktopEntry(final JNLPFile file);

    boolean existsMenuEntry(final JNLPFile file);

    default boolean supportsDesktopEntry() {
        return true;
    }

    default boolean supportsMenuEntry() {
        return true;
    }

    static Optional<MenuAndDesktopEntriesFactory> forCurrentOs() {
        if(Objects.equals(OperationSystem.MAC64, OperationSystem.getLocalSystem())) {
            return Optional.of(new MacEntryFactory());
        }
        if(Objects.equals(OperationSystem.WIN32, OperationSystem.getLocalSystem()) || Objects.equals(OperationSystem.WIN64, OperationSystem.getLocalSystem())) {
            return Optional.of(new WinEntryFactory());
        }
        if(Objects.equals(OperationSystem.LINUX32, OperationSystem.getLocalSystem()) || Objects.equals(OperationSystem.LINUX64, OperationSystem.getLocalSystem())) {
            return Optional.of(new LinuxEntryFactory());
        }
        return Optional.empty();
    }

}
