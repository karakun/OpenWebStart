package com.openwebstart.jvm.runtimes;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ...
 */
public class Vendor {

    private static final Map<String, Vendor> knownVendors = new HashMap<>();

    public static Vendor fromString(final String name) {
        Assert.requireNonBlank(name, "name");
        return knownVendors.computeIfAbsent(name, Vendor::new);
    }

    private final String name;

    private Vendor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vendor vendor = (Vendor) o;
        return Objects.equals(name, vendor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
