package com.openwebstart.install4j;

import com.install4j.api.launcher.Variables;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Install4JConfiguration {

    private final static String LOCK_SUFFIX = ".locked";

    private final static Install4JConfiguration INSTANCE = new Install4JConfiguration();

    private final Lock installerVariableLock = new ReentrantLock();

    private Install4JConfiguration() {
    }

    private Optional<Object> getInstallerVariable​(final String variableName) {
        Assert.requireNonBlank(variableName, "variableName");
        installerVariableLock.lock();
        try {
            final Object value = Variables.getInstallerVariable(variableName);
            return Optional.ofNullable(value);
        } finally {
            installerVariableLock.unlock();
        }
    }

    public Optional<String> getInstallerVariableAsString​(final String variableName) {
        return getInstallerVariable​(variableName).map(v -> v.toString());
    }

    public Optional<Boolean> getInstallerVariableAsBoolean​(final String variableName) {
        return getInstallerVariableAsString​(variableName).map(v -> Boolean.parseBoolean(v));
    }

    public Optional<Integer> getInstallerVariableAsInt​(final String variableName) {
        return getInstallerVariableAsString​(variableName).map(v -> Integer.parseInt(v));
    }

    public Optional<Long> getInstallerVariableAsLong​(final String variableName) {
        return getInstallerVariableAsString​(variableName).map(v -> Long.parseLong(v));
    }

    public String getInstallerVariableAsString​(final String variableName, final String defaultValue) {
        return getInstallerVariableAsString​(variableName).orElse(defaultValue);
    }

    public boolean getInstallerVariableAsBoolean​(final String variableName, final boolean defaultValue) {
        return getInstallerVariableAsBoolean​(variableName).orElse(defaultValue);
    }

    public int getInstallerVariableAsInt​(final String variableName, final int defaultValue) {
        return getInstallerVariableAsInt​(variableName).orElse(defaultValue);
    }

    public long getInstallerVariableAsLong​(final String variableName, final long defaultValue) {
        return getInstallerVariableAsLong​(variableName).orElse(defaultValue);
    }

    public boolean isVariableLocked(final String variableName) {
        return getInstallerVariableAsBoolean​(variableName + LOCK_SUFFIX, false);
    }

    public static Install4JConfiguration getInstance() {
        return INSTANCE;
    }
}
