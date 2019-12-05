package com.openwebstart.proxy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CidrUtilsTest {

    @Test
    void testIsInRange() {
        //given:
        final String cidrNotation = "169.254.0.0/16";
        final String ipAddress = "169.254.0.1";

        //than
        Assertions.assertTrue(CidrUtils.isInRange(cidrNotation, ipAddress));
    }
}