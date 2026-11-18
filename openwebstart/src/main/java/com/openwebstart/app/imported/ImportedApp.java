package com.openwebstart.app.imported;

import java.io.File;
import java.util.Locale;

/**
 * A local .jar or .jnlp file that was imported by the user together with the Java runtime it should be started with.
 */
public class ImportedApp {

    public enum Type {
        JAR, JNLP;

        public static Type forFile(final File file) {
            return file.getName().toLowerCase(Locale.ROOT).endsWith(".jnlp") ? JNLP : JAR;
        }
    }

    private final String id;
    private final String name;
    private final File file;
    private final Type type;
    private final String javaHome;
    private final String javaLabel;

    /**
     * @param javaHome  java home of the runtime to use or null for the automatic selection
     * @param javaLabel human readable description of the runtime (may be null)
     */
    public ImportedApp(final String id, final String name, final File file, final Type type, final String javaHome, final String javaLabel) {
        this.id = id;
        this.name = name;
        this.file = file;
        this.type = type;
        this.javaHome = javaHome;
        this.javaLabel = javaLabel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public Type getType() {
        return type;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getJavaLabel() {
        return javaLabel;
    }

    public ImportedApp withName(final String newName) {
        return new ImportedApp(id, newName, file, type, javaHome, javaLabel);
    }

    public ImportedApp withJava(final String newJavaHome, final String newJavaLabel) {
        return new ImportedApp(id, name, file, type, newJavaHome, newJavaLabel);
    }
}
