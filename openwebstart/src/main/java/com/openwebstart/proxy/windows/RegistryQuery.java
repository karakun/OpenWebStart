package com.openwebstart.proxy.windows;

import com.openwebstart.util.ProcessResult;
import com.openwebstart.util.ProcessUtil;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

class RegistryQuery {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryQuery.class);

    static Map<String, RegistryValue> getAllValuesForKey(final String key) throws Exception {
        final ProcessBuilder processBuilder = new ProcessBuilder("reg", "query", "\"" + key + "\"");
        final ProcessResult processResult = ProcessUtil.runProcess(processBuilder, 5, TimeUnit.SECONDS);
        if (processResult.wasUnsuccessful()) {
            LOG.debug("The reg process printed the following content on the error out: {}", processResult.getErrorOut());
            throw new RuntimeException("failed to execute reg binary");
        }
        return getRegistryValuesFromLines(key, processResult.getStandardOutLines());
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
}
