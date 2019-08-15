package com.openwebstart.jvm.vendor;

import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeManagerConstants;
import net.adoptopenjdk.icedteaweb.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

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
        if(Objects.equals(name, RuntimeManagerConstants.VENDOR_ANY)) {
            return RuntimeManagerConstants.VENDOR_ANY;
        }
        List<VendorResolver> possibleResolvers = resolver.stream()
                .filter(r -> r.isVendor(name))
                .collect(Collectors.toList());

        if(possibleResolvers.isEmpty()) {
            return UNKNOWN_VENDOR;
        }
        if(possibleResolvers.size() > 1) {
            throw new IllegalStateException("More than 1 possible vendor for '" + name + "'");
        }
        return possibleResolvers.get(0).getVendorName();
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
