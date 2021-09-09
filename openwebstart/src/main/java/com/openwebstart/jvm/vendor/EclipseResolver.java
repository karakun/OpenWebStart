package com.openwebstart.jvm.vendor;

import static com.openwebstart.jvm.runtimes.Vendor.ECLIPSE;

public class EclipseResolver extends BasicVendorResolver {

    public EclipseResolver() {
        super(ECLIPSE, "Temurin", "Eclipse Temurin");
    }
}
