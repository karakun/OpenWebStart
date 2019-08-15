package com.openwebstart.jvm;

import com.openwebstart.jvm.runtimes.Vendor;

public interface RuntimeManagerConstants {

    String JSON_STORE_FILENAME = "cache.json";

    Vendor VENDOR_ANY = Vendor.fromString("*");

    Vendor VENDOR_ORACLE = Vendor.fromString("Oracle Corporation");

    Vendor VENDOR_AMAZON = Vendor.fromString("Amazon.com Inc.");

    Vendor VENDOR_BELLSOFT = Vendor.fromString("BellSoft");

    Vendor VENDOR_ADOPT = Vendor.fromString("AdoptOpenJDK");
}
