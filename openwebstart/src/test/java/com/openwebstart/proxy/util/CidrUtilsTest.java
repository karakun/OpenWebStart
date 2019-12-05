package com.openwebstart.proxy.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CidrUtilsTest {

    @ParameterizedTest
    @CsvFileSource(resources = "cidr_test.csv", numLinesToSkip = 1)
    void testIsInRange(final String cidrNotation, final String lastBeforeRange, final String firstInRange, final String someInRange, final String lastInRange, final String firstAfterRange) {
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
