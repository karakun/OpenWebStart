package com.openwebstart.jvm.vendor;

import java.util.Arrays;
import java.util.List;

public class AdoptResolver extends BasicVendorResolver {

    public AdoptResolver() {
        super("AdoptOpenJDK", Arrays.asList("adopt", "AdoptOpenJDK"));
    }
}
