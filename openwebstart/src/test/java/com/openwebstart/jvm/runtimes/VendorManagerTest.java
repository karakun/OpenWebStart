package com.openwebstart.jvm.runtimes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static com.openwebstart.jvm.runtimes.Vendor.ADOPT;
import static com.openwebstart.jvm.runtimes.Vendor.AMAZON;
import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;
import static com.openwebstart.jvm.runtimes.Vendor.BELLSOFT;
import static com.openwebstart.jvm.runtimes.Vendor.ORACLE;

public class VendorManagerTest {

    private static final Vendor KARAKUN = new Vendor("Karakun");

    @Test
    public void testFromString() {

        final BiConsumer<Vendor, String> checkCall = (expectedVendor, givenName) -> {
            final Vendor vendor = Vendor.fromString(givenName);
            Assertions.assertEquals(expectedVendor, vendor, "Wrong vendor returned for name: " + givenName);
        };

        checkCall.accept(ORACLE, "oracle");
        checkCall.accept(ORACLE, "Oracle");
        checkCall.accept(ADOPT, "adopt");
        checkCall.accept(AMAZON, "amazon");
        checkCall.accept(AMAZON, "amazon inc.");
        checkCall.accept(AMAZON, "Amazon Inc.");
        checkCall.accept(BELLSOFT, "liberica");
        checkCall.accept(BELLSOFT, "Bellsoft LIBERICA");
        checkCall.accept(BELLSOFT, "BELLsoFT");
        checkCall.accept(KARAKUN, "Karakun");

        checkCall.accept(ORACLE, "oracle ");
        checkCall.accept(ORACLE, "     Oracle");
        checkCall.accept(ADOPT, "adopt     ");
        checkCall.accept(AMAZON, "       amazon             ");
        checkCall.accept(AMAZON, "    amazon inc.     ");
        checkCall.accept(AMAZON, "   Amazon Inc.    ");
        checkCall.accept(BELLSOFT, "   liberica     ");
        checkCall.accept(BELLSOFT, "    Bellsoft LIBERICA    ");
        checkCall.accept(BELLSOFT, "   BELLsoFT   ");
        checkCall.accept(KARAKUN, "     Karakun     ");
    }

    @Test
    public void testFromStringOrAny() {

        final BiConsumer<Vendor, String> checkCall = (expectedVendor, givenName) -> {
            final Vendor vendor = Vendor.fromStringOrAny(givenName);
            Assertions.assertEquals(expectedVendor, vendor, "Wrong vendor returned for name: " + givenName);
        };
        checkCall.accept(ANY_VENDOR, "");
        checkCall.accept(ANY_VENDOR, "    ");
        checkCall.accept(ANY_VENDOR, null);
        checkCall.accept(ORACLE, "oracle");
        checkCall.accept(ORACLE, "Oracle");
        checkCall.accept(ADOPT, "adopt");
        checkCall.accept(AMAZON, "amazon");
        checkCall.accept(AMAZON, "amazon inc.");
        checkCall.accept(AMAZON, "Amazon Inc.");
        checkCall.accept(BELLSOFT, "liberica");
        checkCall.accept(BELLSOFT, "Bellsoft LIBERICA");
        checkCall.accept(BELLSOFT, "BELLsoFT");
        checkCall.accept(KARAKUN, "Karakun");

        checkCall.accept(ORACLE, "oracle ");
        checkCall.accept(ORACLE, "     Oracle");
        checkCall.accept(ADOPT, "adopt     ");
        checkCall.accept(AMAZON, "       amazon             ");
        checkCall.accept(AMAZON, "    amazon inc.     ");
        checkCall.accept(AMAZON, "   Amazon Inc.    ");
        checkCall.accept(BELLSOFT, "   liberica     ");
        checkCall.accept(BELLSOFT, "    Bellsoft LIBERICA    ");
        checkCall.accept(BELLSOFT, "   BELLsoFT   ");
        checkCall.accept(KARAKUN, "     Karakun     ");
    }
}
