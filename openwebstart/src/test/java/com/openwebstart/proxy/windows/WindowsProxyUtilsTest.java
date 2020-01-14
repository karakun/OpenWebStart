package com.openwebstart.proxy.windows;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.pac.PacBasedProxyProvider;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.openwebstart.proxy.windows.RegistryValueType.REG_DWORD;
import static com.openwebstart.proxy.windows.RegistryValueType.REG_SZ;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.AUTO_CONFIG_URL_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_ENABLED_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_REGISTRY_VAL;

class WindowsProxyUtilsTest {

    @Test
    void createInternalProxyWithPac() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        final String pacUrl = WindowsProxyUtilsTest.class.getResource("simple-pac.js").toExternalForm();
        proxyRegistryEntries.put(AUTO_CONFIG_URL_VAL, new RegistryValue(AUTO_CONFIG_URL_VAL, REG_SZ, pacUrl));

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, proxyRegistryEntries);

        //than
        Assertions.assertNotNull(proxyProvider);
        Assertions.assertTrue(proxyProvider instanceof PacBasedProxyProvider);
        Assertions.assertEquals(Collections.singletonList(Proxy.NO_PROXY), proxyProvider.select(new URI("http://some-url")));
    }

    @Test
    void createInternalProxyWithoutPac() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(AUTO_CONFIG_URL_VAL, new RegistryValue(AUTO_CONFIG_URL_VAL, REG_SZ, null));
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x0"));

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, proxyRegistryEntries);

        //than
        Assertions.assertNotNull(proxyProvider);
        Assertions.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }

    @Test
    void createInternalProxyWithoutPac2() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x0"));

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, proxyRegistryEntries);

        //than
        Assertions.assertNotNull(proxyProvider);
        Assertions.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }

    @Test
    void createInternalProxyWithoutProxyUrl() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x1"));

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, proxyRegistryEntries);

        //than
        Assertions.assertNotNull(proxyProvider);
        Assertions.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }

    @Test
    void createInternalProxyWithoutProxyUrl2() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x1"));
        proxyRegistryEntries.put(PROXY_SERVER_REGISTRY_VAL, new RegistryValue(PROXY_SERVER_REGISTRY_VAL, REG_SZ, null));

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, proxyRegistryEntries);

        //than
        Assertions.assertNotNull(proxyProvider);
        Assertions.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }
}