package com.openwebstart.proxy.windows.registry;

import java.util.Objects;

public class RegistryValue {

    private final String name;

    private final RegistryValueType type;

    private final String value;

    public RegistryValue(final String name, final RegistryValueType type, final String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public RegistryValueType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean getValueAsBoolean() {
        if (type != RegistryValueType.REG_DWORD) {
            throw new IllegalStateException("Can not extract boolean value for value type " + type);
        }
        return Objects.equals(value, "0x1");
    }
}
