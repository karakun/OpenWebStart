package com.openwebstart.jvm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Emulate the -XshowSettings:properties nonstandard option on non-Oracle and pre-1.8 JVM versions.
 * This class should be compiled using pre-1.8 source and target settings.
 * Currently, it is written to conform the 1.6 language level.
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI">
 *     Oracle Java SE 8 Tools Reference java Nonstandard Options</a>
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class SystemPropertiesPrinter {

    private static final int CR_BYTE_VALUE = 10;
    private static final int LF_BYTE_VALUE = 13;

    private SystemPropertiesPrinter() {
        // Utility class, do not instantiate.
    }

    /**
     * <p>Print the system properties to standard error in a similar way as the following Oracle Java 1.8 command would:</p>
     * {@code java -XshowSettings -version}
     */
    public static void main(String[] args) {
        Properties systemProperties = System.getProperties();
        System.err.println("Property settings:");
        List<String> propertyNames = new ArrayList<String>(systemProperties.stringPropertyNames());
        Collections.sort(propertyNames);
        for (String propertyName : propertyNames) {
            printPropertyValue(propertyName, systemProperties.getProperty(propertyName));
        }
        System.err.println();
    }

    /**
     * Print the property key and value supplied in the parameters using a simple formatting:
     * <ul>
     *     <li>
     *         The line.separator property may contain nonprintable characters (CR and LF),
     *         which are replaced with their respective escape strings (\n and \r).
     *     </li>
     *     <li>If a property contains multiple paths, they are printed on separate lines for readability.</li>
     * </ul>
     */
    private static void printPropertyValue(String propertyName, String propertyValue) {
        System.err.print("    " + propertyName + " = ");
        if ("line.separator".equals(propertyName)) {
            byte[] propertyValueBytes = propertyValue.getBytes();
            for (byte propertyValueByte : propertyValueBytes) {
                switch (propertyValueByte) {
                    case CR_BYTE_VALUE:
                        System.err.print("\\n ");
                        break;
                    case LF_BYTE_VALUE:
                        System.err.print("\\r ");
                        break;
                    default:
                        //noinspection MagicNumber
                        System.err.printf("0x%02X", Integer.valueOf(propertyValueByte & 255));
                }
            }
            System.err.println();
        } else if (!isPath(propertyName)) {
            System.err.println(propertyValue);
        } else {
            String[] paths = propertyValue.split(System.getProperty("path.separator"));
            boolean firstPath = true;
            for (String path : paths) {
                if (firstPath) {
                    System.err.println(path);
                    firstPath = false;
                } else {
                    System.err.println("        " + path);
                }
            }
        }
    }

    /**
     * @param propertyName System property key.
     * @return Does this property contain (zero or more) paths?
     */
    private static boolean isPath(String propertyName) {
        return propertyName.endsWith(".dirs") || propertyName.endsWith(".path");
    }
}
