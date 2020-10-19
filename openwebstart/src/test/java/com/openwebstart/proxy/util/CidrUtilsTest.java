package com.openwebstart.proxy.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

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
}
