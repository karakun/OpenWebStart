package com.openwebstart.proxy.mac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class ScutilParserTest {

    @Test
    public void testParse() {

        //given:
        final List<String> lines = new ArrayList<>();
        try (final Scanner sc = new Scanner(ScutilParserTest.class.getResourceAsStream("out1.txt"))) {
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        }

        //when:
        final MacProxySettings proxySettings = ScutilUtil.parse(lines);

        //then:
        Assertions.assertNotNull(proxySettings);
        Assertions.assertTrue(proxySettings.isHttpEnabled());
        Assertions.assertEquals("example.proxy", proxySettings.getHttpHost());
        Assertions.assertEquals(80, proxySettings.getHttpPort());
        Assertions.assertTrue(proxySettings.isHttpsEnabled());
        Assertions.assertEquals("example.https.proxy", proxySettings.getHttpsHost());
        Assertions.assertEquals(88, proxySettings.getHttpsPort());
        Assertions.assertFalse(proxySettings.isFtpEnabled());
        Assertions.assertFalse(proxySettings.isSocksEnabled());
        Assertions.assertFalse(proxySettings.isAutoDiscoveryEnabled());
        Assertions.assertFalse(proxySettings.isAutoConfigEnabled());
        Assertions.assertFalse(proxySettings.isExcludeSimpleHostnames());
    }

    @Test
    public void testParseWithList() {

        //given:
        final List<String> lines = new ArrayList<>();
        try (final Scanner sc = new Scanner(ScutilParserTest.class.getResourceAsStream("out2.txt"))) {
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        }

        //when:
        final MacProxySettings proxySettings = ScutilUtil.parse(lines);

        //then:
        Assertions.assertNotNull(proxySettings);
        Assertions.assertFalse(proxySettings.getExceptionList().isEmpty());
        Assertions.assertTrue(proxySettings.getExceptionList().contains("*.local"));
        Assertions.assertTrue(proxySettings.getExceptionList().contains("169.254/16"));
    }
}