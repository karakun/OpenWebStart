package com.openwebstart.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FilenameUtil {

    private static final int MAX_FILENAME_LENGHT = 64;

    private static final List<Character> INVALID_AND_UNWANTED_CHARACTERS = Arrays.asList('\\', '/', ':', '*', '?', '"', '<', '>', '|', '\0', ',', ' ');

    public static String toSimplifiedFileName(final String name) {
        if (name == null) {
            return toSimplifiedFileName(UUID.randomUUID().toString());
        }

        final String phase1 = name.toLowerCase().trim();
        if (phase1.isEmpty()) {
            return toSimplifiedFileName(UUID.randomUUID().toString());
        }
        if (phase1.length() > MAX_FILENAME_LENGHT) {
            return toSimplifiedFileName(phase1.substring(0, MAX_FILENAME_LENGHT - 1));
        }

        final StringBuilder phase2Builder = new StringBuilder();

        boolean lastCharReplaced = false;
        for (int i = 0; i < phase1.length(); i++) {
            if (phase1.charAt(i) == '-') {
                if (!lastCharReplaced) {
                    phase2Builder.append(phase1.charAt(i));
                }
                lastCharReplaced = true;
            } else if (INVALID_AND_UNWANTED_CHARACTERS.contains(phase1.charAt(i))) {
                if (!lastCharReplaced) {
                    phase2Builder.append("-");
                }
                lastCharReplaced = true;
            } else {
                lastCharReplaced = false;
                phase2Builder.append(phase1.charAt(i));
            }
        }
        return phase2Builder.toString();
    }
}
