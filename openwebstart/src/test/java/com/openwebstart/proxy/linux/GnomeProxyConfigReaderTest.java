package com.openwebstart.proxy.linux;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.MANUAL;
import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.NO_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.PAC;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_AUTOCONFIG_URL;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_FTP_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_FTP_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTPS_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTPS_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTP_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_IGNORE_HOSTS;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_MODE;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_SOCKS_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_SOCKS_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ...
 */
class GnomeProxyConfigReaderTest {

    public static final String SAMPLE_OUTPUT = "" +
            "org.gnome.system.proxy use-same-proxy true\n" +
            "org.gnome.system.proxy mode 'none'\n" +
            "org.gnome.system.proxy autoconfig-url '/tmp/foo/pac.js'\n" +
            "org.gnome.system.proxy ignore-hosts ['localhost', '127.0.0.0/8', '::1']\n" +
            "org.gnome.system.proxy.ftp host ''\n" +
            "org.gnome.system.proxy.ftp port 0\n" +
            "org.gnome.system.proxy.socks host ''\n" +
            "org.gnome.system.proxy.socks port 0\n" +
            "org.gnome.system.proxy.http host '127.0.0.2'\n" +
            "org.gnome.system.proxy.http port 8080\n" +
            "org.gnome.system.proxy.http use-authentication false\n" +
            "org.gnome.system.proxy.http authentication-password ''\n" +
            "org.gnome.system.proxy.http authentication-user ''\n" +
            "org.gnome.system.proxy.http enabled false\n" +
            "org.gnome.system.proxy.https host ''\n" +
            "org.gnome.system.proxy.https port 0\n";

    @Test
    void parseGnomeSettings() {
        // when
        final Map<String, String> result = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);

        // then
        assertEquals("'none'", result.get(GNOME_PROXY_MODE));
        assertEquals("'/tmp/foo/pac.js'", result.get(GNOME_PROXY_AUTOCONFIG_URL));
        assertEquals("['localhost', '127.0.0.0/8', '::1']", result.get(GNOME_PROXY_IGNORE_HOSTS));
        assertEquals("8080", result.get(GNOME_PROXY_HTTP_PORT));
    }

    @Test
    void convertToSettingsForNoProxy() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(NO_PROXY, result.getMode());
    }

    @Test
    void convertToSettingsForPacProxy() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'auto'");
        values.put(GNOME_PROXY_AUTOCONFIG_URL, "'file:///tmp/foo/pac.js'");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(PAC, result.getMode());
        assertEquals(new URL("file:///tmp/foo/pac.js"), result.getAutoConfigUrl());
    }

    @Test
    void convertToSettingsForManualProxyOnlyHttpSet() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(MANUAL, result.getMode());
        assertFalse(result.isSocksEnabled());
        assertFalse(result.isFtpEnabled());
        assertTrue(result.isHttpEnabled());
        assertTrue(result.isHttpsEnabled());
        assertEquals("127.0.0.2", result.getHttpHost());
        assertEquals("127.0.0.2", result.getHttpsHost());
        assertEquals(8080, result.getHttpPort());
        assertEquals(8080, result.getHttpsPort());
    }

    @Test
    void convertToSettingsForManualProxyOnlySocksSet() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");
        values.put(GNOME_PROXY_HTTP_PORT, "0");
        values.put(GNOME_PROXY_SOCKS_HOST, "'127.1.1.1'");
        values.put(GNOME_PROXY_SOCKS_PORT, "8081");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(MANUAL, result.getMode());
        assertTrue(result.isSocksEnabled());
        assertTrue(result.isFtpEnabled());
        assertTrue(result.isHttpEnabled());
        assertTrue(result.isHttpsEnabled());
        assertEquals("127.1.1.1", result.getSocksHost());
        assertEquals("127.1.1.1", result.getFtpHost());
        assertEquals("127.1.1.1", result.getHttpHost());
        assertEquals("127.1.1.1", result.getHttpsHost());
        assertEquals(8081, result.getSocksPort());
        assertEquals(8081, result.getFtpPort());
        assertEquals(8081, result.getHttpPort());
        assertEquals(8081, result.getHttpsPort());
    }

    @Test
    void convertToSettingsForManualProxyAllSet() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");
        values.put(GNOME_PROXY_SOCKS_HOST, "'127.1.1.1'");
        values.put(GNOME_PROXY_SOCKS_PORT, "8081");
        values.put(GNOME_PROXY_FTP_HOST, "'127.1.1.2'");
        values.put(GNOME_PROXY_FTP_PORT, "8082");
        values.put(GNOME_PROXY_HTTPS_HOST, "'127.1.1.3'");
        values.put(GNOME_PROXY_HTTPS_PORT, "8083");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(MANUAL, result.getMode());
        assertTrue(result.isSocksEnabled());
        assertTrue(result.isFtpEnabled());
        assertTrue(result.isHttpEnabled());
        assertTrue(result.isHttpsEnabled());
        assertEquals("127.1.1.1", result.getSocksHost());
        assertEquals("127.1.1.2", result.getFtpHost());
        assertEquals("127.0.0.2", result.getHttpHost());
        assertEquals("127.1.1.3", result.getHttpsHost());
        assertEquals(8081, result.getSocksPort());
        assertEquals(8082, result.getFtpPort());
        assertEquals(8080, result.getHttpPort());
        assertEquals(8083, result.getHttpsPort());
    }

    @Test
    void convertToSettingsForDefaultExclusionList() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(Arrays.asList("localhost", "127.0.0.0/8", "::1"), result.getExceptionList());
        assertTrue(result.isLocalhostExcluded());
    }

    @Test
    void convertToSettingsForManualExclusionList() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");
        values.put(GNOME_PROXY_IGNORE_HOSTS, "['google.com', '192.168.1.1']");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        assertEquals(Arrays.asList("google.com", "192.168.1.1"), result.getExceptionList());
        assertFalse(result.isLocalhostExcluded());
    }

}
