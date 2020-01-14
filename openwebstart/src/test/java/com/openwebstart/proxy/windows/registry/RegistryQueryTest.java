package com.openwebstart.proxy.windows.registry;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RegistryQueryTest {

    @Test
    public void getRegistryValuesFromLines() throws Exception {
        //Given
        final String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        final String content = IOUtils.readContentAsUtf8String(RegistryQueryTest.class.getResourceAsStream("reg_query.out"));
        final List<String> lines = Arrays.asList(content.split(System.lineSeparator()));

        //When
        final Map<String, RegistryValue> values = RegistryQuery.getRegistryValuesFromLines(key, lines);

        //Then
        Assertions.assertEquals(14, values.size());

        Assertions.assertNotNull(values.get("DisableCachingOfSSLPages"));
        Assertions.assertEquals(RegistryValueType.REG_DWORD, values.get("DisableCachingOfSSLPages").getType());
        Assertions.assertEquals("0x0", values.get("DisableCachingOfSSLPages").getValue());
        Assertions.assertFalse(values.get("DisableCachingOfSSLPages").getValueAsBoolean());

        Assertions.assertNotNull(values.get("MigrateProxy"));
        Assertions.assertEquals(RegistryValueType.REG_DWORD, values.get("MigrateProxy").getType());
        Assertions.assertEquals("0x1", values.get("MigrateProxy").getValue());
        Assertions.assertTrue(values.get("MigrateProxy").getValueAsBoolean());

        Assertions.assertNotNull(values.get("ProxyServer"));
        Assertions.assertEquals(RegistryValueType.REG_SZ, values.get("ProxyServer").getType());
        Assertions.assertEquals("loooocalhost:80", values.get("ProxyServer").getValue());

        Assertions.assertNotNull(values.get("AutoConfigURL"));
        Assertions.assertEquals(RegistryValueType.REG_SZ, values.get("AutoConfigURL").getType());
        Assertions.assertEquals("huhu", values.get("AutoConfigURL").getValue());
    }

    @Test
    public void getRegistryValuesFromLinesWithNullValue() throws Exception {
        //Given
        final String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        final String content = IOUtils.readContentAsUtf8String(RegistryQueryTest.class.getResourceAsStream("reg_query_with_null_value.out"));
        final List<String> lines = Arrays.asList(content.split(System.lineSeparator()));

        //When
        final Map<String, RegistryValue> values = RegistryQuery.getRegistryValuesFromLines(key, lines);

        //Then
        Assertions.assertEquals(1, values.size());

        Assertions.assertNotNull(values.get("AutoConfigURL"));
        Assertions.assertEquals(RegistryValueType.REG_SZ, values.get("AutoConfigURL").getType());
        Assertions.assertNull(values.get("AutoConfigURL").getValue());
    }

}
