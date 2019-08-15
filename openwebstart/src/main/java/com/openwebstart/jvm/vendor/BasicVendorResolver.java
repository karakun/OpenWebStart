package com.openwebstart.jvm.vendor;

import com.openwebstart.jvm.runtimes.Vendor;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;

import java.util.Arrays;
import java.util.List;

public class BasicVendorResolver implements VendorResolver {

    private final Vendor vendor;

    private final List<String> matchingNames;

    public BasicVendorResolver(final Vendor vendor, String... matchingNames) {
        this.vendor = Assert.requireNonNull(vendor, "vendor");
        this.matchingNames = Arrays.asList(matchingNames);
    }

    @Override
    public Vendor getVendor() {
        return vendor;
    }

    @Override
    public boolean isVendor(final String name) {
        if(StringUtils.isBlank(name)) {
            return false;
        }
        return matchingNames.stream().anyMatch(name::equalsIgnoreCase);
    }
}
