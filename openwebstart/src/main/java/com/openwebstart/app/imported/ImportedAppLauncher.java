package com.openwebstart.app.imported;

import com.openwebstart.jvm.JavaRuntimeSelectorAccess;
import com.openwebstart.launcher.PhaseTwoWebStartLauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts an imported application with the Java runtime chosen by the user.
 * This is the entry point used by the desktop shortcuts: {@code ImportedAppLauncher <appId>}.
 */
public class ImportedAppLauncher {

    public static void main(final String... args) {
        try {
            start(args);
        } catch (final Throwable t) {
            // there is no console when started from a shortcut: write the reason to the log file
            if (args.length == 1) {
                try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(ImportedAppStore.logFile(args[0]), true))) {
                    t.printStackTrace(w);
                } catch (final IOException ignored) {
                    // nothing more we can do
                }
            }
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void start(final String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: ImportedAppLauncher <appId>");
            System.exit(2);
        }
        final ImportedApp app = ImportedAppStore.getInstance().find(args[0]).orElse(null);
        if (app == null) {
            System.err.println("Unknown imported application: " + args[0]);
            System.exit(3);
            return;
        }
        if (!app.getFile().exists()) {
            System.err.println("File does not exist: " + app.getFile());
            System.exit(4);
        }

        if (app.getType() == ImportedApp.Type.JAR) {
            final List<String> cmd = new ArrayList<>();
            cmd.add(javaExecutable(app.getJavaHome() != null ? app.getJavaHome() : System.getProperty("java.home")));
            cmd.add("-jar");
            cmd.add(app.getFile().getAbsolutePath());
            new ProcessBuilder(cmd).directory(app.getFile().getParentFile())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(ImportedAppStore.logFile(app.getId())))
                    .start();
        } else {
            if (app.getJavaHome() != null) {
                System.setProperty(JavaRuntimeSelectorAccess.FORCED_JAVA_HOME_PROPERTY, app.getJavaHome());
            }
            PhaseTwoWebStartLauncher.main(app.getFile().getAbsolutePath());
        }
    }

    /**
     * The class path of the running JVM with every entry made absolute, so that it works from any working directory.
     */
    private static String absoluteClassPath() {
        final String separator = File.pathSeparator;
        final StringBuilder result = new StringBuilder();
        for (final String entry : System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(separator))) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(new File(entry).getAbsolutePath());
        }
        return result.toString();
    }

    public static String javaExecutable(final String javaHome) {
        final boolean win = System.getProperty("os.name").toLowerCase().contains("win");
        return new File(javaHome, "bin/" + (win ? "java.exe" : "java")).getPath();
    }

    /**
     * Command line that starts this launcher for the given app in a new JVM (the one OpenWebStart itself runs in).
     */
    public static List<String> commandFor(final ImportedApp app) {
        final List<String> cmd = new ArrayList<>();
        cmd.add(javaExecutable(System.getProperty("java.home")));
        cmd.add("-Dapple.awt.UIElement=true");
        cmd.add("-cp");
        cmd.add(absoluteClassPath());
        cmd.add(ImportedAppLauncher.class.getName());
        cmd.add(app.getId());
        return cmd;
    }
}
