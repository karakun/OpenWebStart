/*
 * Copyright 2019 Karakun AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
