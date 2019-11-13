package com.openwebstart.jvm.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.util.ArrayList;
import java.util.List;

public class JvmVersionUtils {

    private static final int JAVA_9 = 9;

    /**
     * Converts a string to a version-id assuming it is a JVM version.
     * <p>
     * Valid JVM versions are either between 1.0 and 1.9 (excluding 1.9)
     * or are greater than or equal to 9.0.
     * Versions between 2.0 and 9.0 (excluding 9.0) are sanitized and mapped
     * to a version 1.x
     *
     * @param version the string representing the JVM version
     * @throws IllegalArgumentException if the string is not a valid JVM version.
     */
    public static VersionId fromString(final String version) {
        Assert.requireNonBlank(version, "version");
        final String[] tuples = version.split(JNLPVersionPatterns.REGEXP_SEPARATOR);

        final List<Integer> numericPrefixTuples = getNumericPrefixTuples(tuples);
        final int numTuples = numericPrefixTuples.size();

        if (numTuples == 0) {
            throw new IllegalArgumentException("Not a valid JVM version: " + version);
        }

        final int majorVersion = numericPrefixTuples.get(0);
        if (majorVersion < 1) {
            throw new IllegalArgumentException("Not a valid JVM version: " + version);
        } else if (majorVersion == 1) {
            if (numTuples == 1) {
                throw new IllegalArgumentException("Not a valid JVM version: " + version);
            }
            final int minorVersion = numericPrefixTuples.get(1);
            if (minorVersion >= JAVA_9) {
                throw new IllegalArgumentException("Not a valid JVM version: " + version);
            }
        } else if (majorVersion < JAVA_9) {
            // sanitize version by prefixing "1."
            return fromString("1." + version);
        }
        return VersionId.fromString(version);
    }

    public static VersionString fromJnlp(VersionString jnlpVersion) {
        if (jnlpVersion.isExactVersion()) {
            final String version = jnlpVersion.toString();
            final String[] tuples = version.split(JNLPVersionPatterns.REGEXP_SEPARATOR);
            final int numericTuplesSize = getNumericPrefixTuples(tuples).size();
            if (tuples.length == numericTuplesSize && (numericTuplesSize == 1 || numericTuplesSize == 2)) {
                return VersionString.fromString(version + "*");
            }
        }
        return jnlpVersion;
    }

    private static List<Integer> getNumericPrefixTuples(String[] tuples) {
        final List<Integer> numericTuples = new ArrayList<>();

        for (String tuple : tuples) {
            try {
                numericTuples.add(Integer.parseInt(tuple));
            } catch (NumberFormatException e) {
                return numericTuples;
            }
        }

        return numericTuples;
    }
}
