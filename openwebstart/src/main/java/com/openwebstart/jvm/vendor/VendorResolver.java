package com.openwebstart.jvm.vendor;

public interface VendorResolver {

    String getVendorName();

    boolean isVendor(String name);
}
