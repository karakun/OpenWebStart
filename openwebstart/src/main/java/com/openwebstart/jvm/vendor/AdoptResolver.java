package com.openwebstart.jvm.vendor;

import static com.openwebstart.jvm.runtimes.Vendor.ADOPT;

public class AdoptResolver extends BasicVendorResolver {

    public AdoptResolver() {
        super(ADOPT, "adopt", "AdoptOpenJDK");
    }
}
