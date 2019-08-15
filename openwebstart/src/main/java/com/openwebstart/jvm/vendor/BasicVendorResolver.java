package com.openwebstart.jvm.vendor;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.List;
import java.util.Objects;

public class BasicVendorResolver implements VendorResolver {

    private final String name;

    private final List<String> matchingNames;

    public BasicVendorResolver(final String name, final List<String> matchingNames) {
        this.name = Assert.requireNonBlank(name, "name");
        this.matchingNames = Assert.requireNonNull(matchingNames, "matchingNames");
    }

    @Override
    public String getVendorName() {
        return name;
    }

    @Override
    public boolean isVendor(final String name) {
        Assert.requireNonNull(name, "name");
        return matchingNames.stream()
                .filter(n -> Objects.equals(n.toLowerCase(), name.toLowerCase()))
                .findAny()
                .isPresent();
    }
}
