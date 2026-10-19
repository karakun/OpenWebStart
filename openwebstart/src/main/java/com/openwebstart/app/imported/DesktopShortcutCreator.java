package com.openwebstart.app.imported;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a desktop shortcut that starts an imported application directly with the runtime chosen for it.
 */
public class DesktopShortcutCreator {

    /**
     * @return the created shortcut
     */
    public static File create(final ImportedApp app) throws IOException {
        final File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (!desktop.isDirectory()) {
            throw new IOException("Desktop folder not found: " + desktop);
        }
        final String os = System.getProperty("os.name").toLowerCase();
        final String name = app.getName().replaceAll("[\\\\/:*?\"<>|]", "_");
        final List<String> cmd = ImportedAppLauncher.commandFor(app);
        if (os.contains("mac")) {
            return createMacApp(desktop, name, cmd, app);
        } else if (os.contains("win")) {
            return createWindowsCmd(desktop, name, cmd);
        }
        return createLinuxDesktopFile(desktop, name, cmd);
    }

    private static File createMacApp(final File desktop, final String name, final List<String> cmd, final ImportedApp app) throws IOException {
        final Path contents = new File(desktop, name + ".app").toPath().resolve("Contents");
        final Path macOs = contents.resolve("MacOS");
        Files.createDirectories(macOs);

        final String plist = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
                + "<plist version=\"1.0\"><dict>\n"
                + "<key>CFBundleExecutable</key><string>run</string>\n"
                + "<key>CFBundleName</key><string>" + xml(name) + "</string>\n"
                + "<key>CFBundleIdentifier</key><string>com.openwebstart.imported." + app.getId() + "</string>\n"
                + "<key>CFBundlePackageType</key><string>APPL</string>\n"
                + "</dict></plist>\n";
        Files.write(contents.resolve("Info.plist"), plist.getBytes(StandardCharsets.UTF_8));

        final Path script = macOs.resolve("run");
        Files.write(script, ("#!/bin/bash\nexec " + shellLine(cmd) + "\n").getBytes(StandardCharsets.UTF_8));
        script.toFile().setExecutable(true, false);
        return contents.getParent().toFile();
    }

    private static File createLinuxDesktopFile(final File desktop, final String name, final List<String> cmd) throws IOException {
        final File file = new File(desktop, name + ".desktop");
        final String content = "[Desktop Entry]\nType=Application\nName=" + name + "\nExec=" + desktopEntryLine(cmd) + "\nTerminal=false\n";
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        file.setExecutable(true, false);
        return file;
    }

    private static File createWindowsCmd(final File desktop, final String name, final List<String> cmd) throws IOException {
        final File file = new File(desktop, name + ".cmd");
        final String line = cmd.stream().map(a -> "\"" + a + "\"").collect(Collectors.joining(" "));
        // javaw.exe does not open a console window
        final String content = "@echo off\r\nstart \"\" " + line.replace("\\java.exe\"", "\\javaw.exe\"") + "\r\n";
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * Exec line of a .desktop file: arguments are double quoted, single quotes are not supported by the spec.
     */
    private static String desktopEntryLine(final List<String> cmd) {
        return cmd.stream()
                .map(a -> "\"" + a.replace("\\", "\\\\").replace("\"", "\\\"").replace("`", "\\`").replace("$", "\\$").replace("%", "%%") + "\"")
                .collect(Collectors.joining(" "));
    }

    private static String shellLine(final List<String> cmd) {
        return cmd.stream().map(a -> "'" + a.replace("'", "'\\''") + "'").collect(Collectors.joining(" "));
    }

    private static String xml(final String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
