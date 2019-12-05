package com.openwebstart.proxy.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CidrUtilsTest {

    @Test
    void testIsInRange() {
        //given:
        final String cidrNotation = "169.254/16";

        final String lastBeforeRange = "169.253.255.255";
        final String firstInRange = "169.254.0.0";
        final String someInRange = "169.254.23.55";
        final String lastInRange = "169.254.255.255";
        final String firstAfterRange = "169.255.0.0";

        //than
        assertFalse(CidrUtils.isInRange(cidrNotation, lastBeforeRange));
        assertTrue(CidrUtils.isInRange(cidrNotation, firstInRange));
        assertTrue(CidrUtils.isInRange(cidrNotation, someInRange));
        assertTrue(CidrUtils.isInRange(cidrNotation, lastInRange));
        assertFalse(CidrUtils.isInRange(cidrNotation, firstAfterRange));
    }

    @Test
    void testIsInRange2() {
        //given:
        final String cidrNotation = "169.254.1.80/30";

        final String lastBeforeRange = "169.254.1.79";
        final String firstInRange = "169.254.1.80";
        final String someInRange = "169.254.1.81";
        final String lastInRange = "169.254.1.83";
        final String firstAfterRange = "169.254.1.84";

        //than
        assertFalse(CidrUtils.isInRange(cidrNotation, lastBeforeRange));
        assertTrue(CidrUtils.isInRange(cidrNotation, firstInRange));
        assertTrue(CidrUtils.isInRange(cidrNotation, someInRange));
        assertTrue(CidrUtils.isInRange(cidrNotation, lastInRange));
        assertFalse(CidrUtils.isInRange(cidrNotation, firstAfterRange));
    }
}
