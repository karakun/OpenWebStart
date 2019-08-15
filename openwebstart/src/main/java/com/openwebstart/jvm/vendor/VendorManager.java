package com.openwebstart.jvm.vendor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static VendorManager getInstance() {
        return INSTANCE;
    }
}
