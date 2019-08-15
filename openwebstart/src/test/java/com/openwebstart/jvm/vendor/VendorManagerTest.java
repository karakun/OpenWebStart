package com.openwebstart.jvm.vendor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VendorManagerTest {

    @Test
    public void test1() {
        //given
        final String givenName = "oracle";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Oracle", internalName);
    }

    @Test
    public void test2() {
        //given
        final String givenName = "Oracle";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Oracle", internalName);
    }

    @Test
    public void test3() {
        //given
        final String givenName = "adopt";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("AdoptOpenJDK", internalName);
    }

    @Test
    public void test4() {
        //given
        final String givenName = "amazon";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Amazon Inc.", internalName);
    }

    @Test
    public void test5() {
        //given
        final String givenName = "amazon inc.";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Amazon Inc.", internalName);
    }

    @Test
    public void test6() {
        //given
        final String givenName = "Amazon Inc.";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Amazon Inc.", internalName);
    }

    @Test
    public void test7() {
        //given
        final String givenName = "liberica";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Bellsoft", internalName);
    }

    @Test
    public void test8() {
        //given
        final String givenName = "Bellsoft LIBERICA";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Bellsoft", internalName);
    }

    @Test
    public void test9() {
        //given
        final String givenName = "BELLsoFT";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Bellsoft", internalName);
    }

    @Test
    public void test99() {
        //given
        final String givenName = "Karakun";

        //when
        final String internalName = VendorManager.getInstance().getInternalName(givenName);

        //than
        Assertions.assertNotNull(internalName);
        Assertions.assertEquals("Karakun", internalName);
    }
}
