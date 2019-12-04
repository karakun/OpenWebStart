package com.openwebstart.proxy.mac;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ScutilParserTest {

    @Test
    public void testParse() throws IOException {
        //given:
        final String output = IOUtils.readContentAsUtf8String(ScutilParserTest.class.getResourceAsStream("out1.txt"));

        //when:
        final MacProxySettings proxySettings = ScutilParser.parse(output);

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
}