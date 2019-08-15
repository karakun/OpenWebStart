package com.openwebstart.jvm.vendor;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;

import java.util.List;

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
        if(StringUtils.isBlank(name)) {
            return false;
        }
        return matchingNames.stream().anyMatch(name::equalsIgnoreCase);
    }
}
