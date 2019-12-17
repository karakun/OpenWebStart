package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.vendor.BasicVendorResolver;
import com.openwebstart.jvm.vendor.VendorResolver;
import net.adoptopenjdk.icedteaweb.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Vendor of a Java JDK.
 * <p/>
 * Use the {@link #fromString(String)} whenever possible.
 * The constructor should only be used by implementations of {@link VendorResolver}.
 */
public class Vendor {

    public static final Vendor ANY_VENDOR = new Vendor("*");
    public static final Vendor ORACLE = new Vendor("Oracle Corporation");
    public static final Vendor AMAZON = new Vendor("Amazon.com Inc.");
    public static final Vendor BELLSOFT = new Vendor("BellSoft");
    public static final Vendor ADOPT = new Vendor("AdoptOpenJDK");

    private static final List<VendorResolver> resolver;

    static {
        final List<VendorResolver> loaded = new ArrayList<>();
        loaded.add(new BasicVendorResolver(ANY_VENDOR, ANY_VENDOR.getName()));
        ServiceLoader.load(VendorResolver.class).iterator().forEachRemaining(loaded::add);
        resolver = Collections.unmodifiableList(loaded);
    }

    /**
     * Local cache to return same instance for a given string also in the case of unknown vendors.
     */
    private static final Map<String, Vendor> knownVendors = new HashMap<>();

    /**
     * Normalizes the given name and returns the corresponding vendor.
     * If no vendor with the given name is known a new vendor is created and returned.
     *
     * @param name the name of the vendor
     * @return a vendor, never {@code null}
     */
    public static Vendor fromString(final String name) {
        Assert.requireNonBlank(name, "name");
        return knownVendors.computeIfAbsent(name, Vendor::getVendor);
    }

    private static Vendor getVendor(final String name) {
        List<VendorResolver> possibleResolvers = resolver.stream()
                .filter(r -> r.isVendor(name))
                .collect(Collectors.toList());

        if (possibleResolvers.isEmpty()) {
            return new Vendor(name);
        }
        if (possibleResolvers.size() > 1) {
            throw new IllegalStateException("More than 1 possible vendor for '" + name + "'");
        }
        return possibleResolvers.get(0).getVendor();
    }

    private final String name;

    /**
     * Should only be used by implementations of {@link VendorResolver}.
     *
     * @param name of the vendor
     */
    public Vendor(String name) {
        this.name = Assert.requireNonBlank(name, "name");
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
