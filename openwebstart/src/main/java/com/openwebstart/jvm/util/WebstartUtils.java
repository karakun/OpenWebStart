package com.openwebstart.jvm.util;

import net.adoptopenjdk.icedteaweb.Assert;

public class WebstartUtils {

    public static String convertJavaVersion(final String version) {
        Assert.requireNonBlank(version, "version");
        final String[] versionSplit = version.split("\\.");
        if (versionSplit.length == 0) {
            return "UNKNOWN";
        }
        if (versionSplit.length == 1) {
            final String majorVersion = versionSplit[0];
            try {
                final int majorVersionInt = Integer.parseInt(majorVersion);
                if (majorVersionInt <= 0) {
                    return "UNKNOWN";
                }
                return majorVersionInt + ".0";
            } catch (Exception e) {
                return "UNKNOWN";
            }
        }
        if (versionSplit.length == 3) {
            final String majorVersion = versionSplit[0];
            if (majorVersion.equals("1")) {
                final String realMajorVersion = versionSplit[1];
                try {
                    final int realMajorVersionInt = Integer.parseInt(realMajorVersion);
                    if (realMajorVersionInt <= 0) {
                        return "UNKNOWN";
                    }
                } catch (Exception e) {
                    return "UNKNOWN";
                }
                String minorVersion = versionSplit[2];
                if (minorVersion.startsWith("0_")) {
                    minorVersion = minorVersion.substring(2);
                }
                try {
                    final int minorVersionInt = Integer.parseInt(minorVersion);
                    if (minorVersionInt < 0) {
                        return "UNKNOWN";
                    }
                    return realMajorVersion + "." + minorVersionInt;
                } catch (Exception e) {
                    return "UNKNOWN";
                }
            } else {
                try {
                    final int majorVersionInt = Integer.parseInt(majorVersion);
                    if (majorVersionInt <= 0) {
                        return "UNKNOWN";
                    }
                    String minorVersion = versionSplit[1];
                    try {
                        final int minorVersionInt = Integer.parseInt(minorVersion);
                        if (minorVersionInt < 0) {
                            return "UNKNOWN";
                        }
                        return majorVersionInt + "." + minorVersionInt;
                    } catch (Exception e) {
                        return "UNKNOWN";
                    }
                } catch (Exception e) {
                    return "UNKNOWN";
                }
            }
        }
        return "UNKNOWN";
    }

}
