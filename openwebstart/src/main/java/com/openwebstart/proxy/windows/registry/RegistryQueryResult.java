package com.openwebstart.proxy.windows.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RegistryQueryResult {

    private final Map<String, RegistryValue> proxyRegistryEntries;

    public RegistryQueryResult(final Map<String, RegistryValue> proxyRegistryEntries) {
        this.proxyRegistryEntries = new HashMap<>(proxyRegistryEntries);
    }

    public String getValue(final String name) {
        return Optional.ofNullable(proxyRegistryEntries.get(name))
                .map(RegistryValue::getValue)
                .orElse(null);
    }

    public boolean getValueAsBoolean(final String name) {
        final RegistryValue value = proxyRegistryEntries.get(name);
        if (value == null) {
            return false;
        }
        if (value.getType() != RegistryValueType.REG_DWORD) {
            throw new IllegalStateException("Can not extract boolean value for value type " + value.getType());
        }
        return Objects.equals(value.getValue(), "0x1");
    }
}
