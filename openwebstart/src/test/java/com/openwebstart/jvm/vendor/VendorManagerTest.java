package com.openwebstart.jvm.vendor;

import com.openwebstart.jvm.runtimes.Vendor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.openwebstart.jvm.RuntimeManagerConstants.VENDOR_ADOPT;
import static com.openwebstart.jvm.RuntimeManagerConstants.VENDOR_AMAZON;
import static com.openwebstart.jvm.RuntimeManagerConstants.VENDOR_BELLSOFT;
import static com.openwebstart.jvm.RuntimeManagerConstants.VENDOR_ORACLE;

public class VendorManagerTest {

    @Test
    public void test1() {
        //given
        final String givenName = "oracle";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_ORACLE, vendor);
    }

    @Test
    public void test2() {
        //given
        final String givenName = "Oracle";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_ORACLE, vendor);
    }

    @Test
    public void test3() {
        //given
        final String givenName = "adopt";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_ADOPT, vendor);
    }

    @Test
    public void test4() {
        //given
        final String givenName = "amazon";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_AMAZON, vendor);
    }

    @Test
    public void test5() {
        //given
        final String givenName = "amazon inc.";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_AMAZON, vendor);
    }

    @Test
    public void test6() {
        //given
        final String givenName = "Amazon Inc.";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_AMAZON, vendor);
    }

    @Test
    public void test7() {
        //given
        final String givenName = "liberica";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_BELLSOFT, vendor);
    }

    @Test
    public void test8() {
        //given
        final String givenName = "Bellsoft LIBERICA";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_BELLSOFT, vendor);
    }

    @Test
    public void test9() {
        //given
        final String givenName = "BELLsoFT";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(VENDOR_BELLSOFT, vendor);
    }

    @Test
    public void test99() {
        //given
        final String givenName = "Karakun";

        //when
        final Vendor vendor = VendorManager.getInstance().getVendor(givenName);

        //than
        Assertions.assertNotNull(vendor);
        Assertions.assertEquals(Vendor.fromString("Karakun"), vendor);
    }
}
