package com.openwebstart.jvm.runtimes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.openwebstart.jvm.runtimes.Vendor.ADOPT;
import static com.openwebstart.jvm.runtimes.Vendor.AMAZON;
import static com.openwebstart.jvm.runtimes.Vendor.BELLSOFT;
import static com.openwebstart.jvm.runtimes.Vendor.ORACLE;

public class VendorManagerTest {

    private static final Vendor KARAKUN = new Vendor("Karakun");

    @Test
    public void testFromString() {
        doTest(ORACLE, "oracle");
        doTest(ORACLE, "Oracle");
        doTest(ADOPT, "adopt");
        doTest(AMAZON, "amazon");
        doTest(AMAZON, "amazon inc.");
        doTest(AMAZON, "Amazon Inc.");
        doTest(BELLSOFT, "liberica");
        doTest(BELLSOFT, "Bellsoft LIBERICA");
        doTest(BELLSOFT, "BELLsoFT");
        doTest(KARAKUN, "Karakun");
    }

    private void doTest(Vendor expectedVendor, String givenName) {
        //when
        final Vendor vendor = Vendor.fromString(givenName);

        //than
        Assertions.assertEquals(expectedVendor, vendor, "Wrong vendor returned for name: " + givenName);
    }
}
