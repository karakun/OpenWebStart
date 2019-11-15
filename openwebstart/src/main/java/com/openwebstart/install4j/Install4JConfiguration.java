package com.openwebstart.install4j;

import com.install4j.api.launcher.Variables;
import net.adoptopenjdk.icedteaweb.Assert;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Install4JConfiguration {

    private static final String LOCK_SUFFIX = ".locked";

    private static final Install4JConfiguration INSTANCE = new Install4JConfiguration();

    private final Lock installerVariableLock = new ReentrantLock();

    private Install4JConfiguration() {
    }

    private Optional<Object> getInstallerVariable(final String variableName) {
        Assert.requireNonBlank(variableName, "variableName");
        installerVariableLock.lock();
        try {
            final Object value = Variables.getInstallerVariable(variableName);
            return Optional.ofNullable(value);
        } finally {
            installerVariableLock.unlock();
        }
    }

    public Optional<String> getInstallerVariableAsString(final String variableName) {
        return getInstallerVariable(variableName).map(Object::toString);
    }

    public Optional<Boolean> getInstallerVariableAsBoolean(final String variableName) {
        return getInstallerVariableAsString(variableName).map(Boolean::parseBoolean);
    }

    public Optional<Long> getInstallerVariableAsLong(final String variableName) {
        return getInstallerVariableAsString(variableName).map(Long::parseLong);
    }

    public Optional<Boolean> isVariableLocked(final String variableName) {
        return getInstallerVariableAsBoolean(variableName + LOCK_SUFFIX);
    }

    public static Install4JConfiguration getInstance() {
        return INSTANCE;
    }
}
