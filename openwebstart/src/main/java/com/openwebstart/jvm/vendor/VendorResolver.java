package com.openwebstart.jvm.vendor;

import com.openwebstart.jvm.runtimes.Vendor;

public interface VendorResolver {

    Vendor getVendor();

    boolean isVendor(String name);
}
