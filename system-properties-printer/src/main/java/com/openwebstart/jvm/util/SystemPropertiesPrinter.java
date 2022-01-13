package com.openwebstart.jvm.util;

import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Emulates the -XshowSettings:properties nonstandard option on non-Oracle and pre-1.8 JVM versions. The main
 * difference is that we only print a small subset of the system properties required by OWS
 * JavaRuntimePropertiesDetector. This class should be compiled using pre-1.8 source and target settings.
 * Currently, it is set to conform the 1.6 language level.
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI">
 *     Oracle Java SE 8 Tools Reference java Nonstandard Options</a>
 */
public final class SystemPropertiesPrinter {

    /**
     * Must match the contents of JavaRuntimePropertiesDetector.REQUIRED_PROPS
     */
    @SuppressWarnings("StaticCollection")
    private static final List<String> REQUIRED_PROPS =
            unmodifiableList(asList("java.vendor", "java.version", "os.name", "os.arch"));

    private SystemPropertiesPrinter() {
        // Utility class, do not instantiate.
    }

    public static void main(String[] args) {
        final Properties systemProperties = System.getProperties();
        System.err.println("Property settings:");
        for (final String propertyName : REQUIRED_PROPS) {
            if (systemProperties.containsKey(propertyName)) {
                System.err.println("    " + propertyName + " = " + systemProperties.getProperty(propertyName));
            }
        }
        System.err.println();
    }
}
