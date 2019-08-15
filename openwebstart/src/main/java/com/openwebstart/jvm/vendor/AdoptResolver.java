package com.openwebstart.jvm.vendor;

import static com.openwebstart.jvm.RuntimeManagerConstants.VENDOR_ADOPT;

public class AdoptResolver extends BasicVendorResolver {

    public AdoptResolver() {
        super(VENDOR_ADOPT, "adopt", "AdoptOpenJDK");
    }
}
