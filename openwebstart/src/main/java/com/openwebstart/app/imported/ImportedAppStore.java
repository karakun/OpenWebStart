package com.openwebstart.app.imported;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * Persists the imported applications in a properties file in the user config directory.
 */
public class ImportedAppStore {

    private static final ImportedAppStore INSTANCE = new ImportedAppStore();

    public static ImportedAppStore getInstance() {
        return INSTANCE;
    }

    private ImportedAppStore() {
    }

    private static File storeFile() {
        final String appData = System.getenv("APPDATA");
        final File base = (System.getProperty("os.name").toLowerCase().contains("win") && appData != null)
                ? new File(appData, "icedtea-web")
                : new File(System.getProperty("user.home"), ".config/icedtea-web");
        return new File(base, "ows-imported-apps.properties");
    }

    public synchronized List<ImportedApp> getAll() {
        final Properties p = load();
        final List<ImportedApp> result = new ArrayList<>();
        for (final String key : p.stringPropertyNames()) {
            if (key.endsWith(".file")) {
                final String id = key.substring(0, key.length() - ".file".length());
                final File file = new File(p.getProperty(key));
                result.add(new ImportedApp(
                        id,
                        p.getProperty(id + ".name", file.getName()),
                        file,
                        ImportedApp.Type.valueOf(p.getProperty(id + ".type", ImportedApp.Type.forFile(file).name())),
                        emptyToNull(p.getProperty(id + ".javaHome")),
                        emptyToNull(p.getProperty(id + ".javaLabel"))));
            }
        }
        result.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    public synchronized Optional<ImportedApp> find(final String id) {
        return getAll().stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public synchronized ImportedApp add(final File file) throws IOException {
        final String name = file.getName().replaceFirst("\\.[^.]*$", "");
        final ImportedApp app = new ImportedApp(UUID.randomUUID().toString(), name, file.getAbsoluteFile(), ImportedApp.Type.forFile(file), null, null);
        save(app);
        return app;
    }

    public synchronized void save(final ImportedApp app) throws IOException {
        final Properties p = load();
        p.setProperty(app.getId() + ".name", app.getName());
        p.setProperty(app.getId() + ".file", app.getFile().getPath());
        p.setProperty(app.getId() + ".type", app.getType().name());
        p.setProperty(app.getId() + ".javaHome", app.getJavaHome() == null ? "" : app.getJavaHome());
        p.setProperty(app.getId() + ".javaLabel", app.getJavaLabel() == null ? "" : app.getJavaLabel());
        store(p);
    }

    public synchronized void remove(final ImportedApp app) throws IOException {
        final Properties p = load();
        for (final String key : p.stringPropertyNames()) {
            if (key.startsWith(app.getId() + ".")) {
                p.remove(key);
            }
        }
        store(p);
    }

    private Properties load() {
        final Properties p = new Properties();
        final File f = storeFile();
        if (f.exists()) {
            try (InputStream in = Files.newInputStream(f.toPath())) {
                p.load(in);
            } catch (final IOException ignored) {
                // start with an empty store
            }
        }
        return p;
    }

    private void store(final Properties p) throws IOException {
        final File f = storeFile();
        Files.createDirectories(f.getParentFile().toPath());
        try (OutputStream out = Files.newOutputStream(f.toPath())) {
            p.store(out, "OpenWebStart imported applications");
        }
    }

    private static String emptyToNull(final String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}
