package com.openwebstart.jvm.vendor;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

public class VendorManager {

    private final static String UNKNOWN_VENDOR = "Unknown vendor";

    private final static VendorManager INSTANCE = new VendorManager();

    private final List<VendorResolver> resolver;

    public VendorManager() {
        final List<VendorResolver> loaded = new ArrayList<>();
        ServiceLoader.load(VendorResolver.class).iterator().forEachRemaining(r -> loaded.add(r));
        resolver = Collections.unmodifiableList(loaded);
    }

    public String getInternalName(final String name) {
        return resolver.stream().filter(r -> r.isVendor(name))
                .findAny().map(r -> r.getVendorName())
                .orElse(UNKNOWN_VENDOR);
    }

    public boolean equals(final String nameA, final String nameB) {
        Assert.requireNonNull(nameA, "nameA");
        Assert.requireNonNull(nameB, "nameB");
        return Objects.equals(getInternalName(nameA), getInternalName(nameB));
    }

    public static VendorManager getInstance() {
        return INSTANCE;
    }
}
