package com.openwebstart.util;

import com.install4j.runtime.launcher.util.LauncherUtil;

public class PathQuoteUtil {
    private static final String WHITESPACE = " ";
    private static final String DOUBLE_QUOTE = "\"";

    /**
     * Checks whether a given path string contains spaces and if so, is correctly double quoted.
     *
     * @param original the path string to check
     * @return the correctly quoted path string if needed
     */
    public static String quoteIfRequired(final String original) {
        if (LauncherUtil.isWindows() && original != null && original.contains(WHITESPACE) && !original.startsWith(DOUBLE_QUOTE)) {
            return DOUBLE_QUOTE + original + DOUBLE_QUOTE;
        }
        return original;
    }
}
