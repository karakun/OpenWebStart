package com.openwebstart.proxy.windows;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

class RegistryQuery {

    static Map<String, RegistryValue> getAllValuesForKey(final String key) throws IOException, InterruptedException, ExecutionException {
        final Process start = new ProcessBuilder().command("reg", "query", "\"" + key + "\"")
                .redirectErrorStream(true)
                .start();
        final Future<List<String>> linesFuture = getLines(start.getInputStream());
        final int exitValue = start.waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("Process ended with error code: " + exitValue);
        }
        final List<String> lines = linesFuture.get();

        return getRegistryValuesFromLines(key, lines);
    }

    static Map<String, RegistryValue> getRegistryValuesFromLines(final String key, final List<String> lines) {
        return lines.stream()
                .filter(l -> !l.contains(key))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .map(RegistryQuery::parseSingleLine)
                .collect(Collectors.toMap(RegistryValue::getName, Function.identity()));
    }

    private static RegistryValue parseSingleLine(final String line) {
        final int index = line.indexOf("REG_");
        if (index < 1) {
            throw new IllegalArgumentException("Can not findPreferencesFile type in line: '" + line + "'");
        }
        final String name = line.substring(0, index).trim();
        final String[] typeAndValue = line.substring(index).split("\\s+", 2);
        if (typeAndValue.length != 2) {
            throw new IllegalArgumentException("Can not getPreferences value in line: '" + line + "'");
        }
        final String value = typeAndValue[1].trim();
        try {
            final RegistryValueType type = RegistryValueType.valueOf(typeAndValue[0].trim());
            return new RegistryValue(name, type, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Can not getPreferences type in line: '" + line + "'");
        }
    }

    private static Future<List<String>> getLines(final InputStream src) {
        final CompletableFuture<List<String>> result = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final List<String> lines = new ArrayList<>();
                final Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    lines.add(sc.nextLine());
                }
                result.complete(lines);
            } catch (final Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

}
