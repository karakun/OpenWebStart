package com.openwebstart.proxy.windows;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.openwebstart.proxy.windows.WindowsProxyConstants.AUTO_CONFIG_URL_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_ENABLED_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_REGISTRY_KEY;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_OVERRIDE_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxy {

    private static void printProxySettings() throws Exception {
        final RegistryQueryResult queryResult = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
        if (queryResult.getValueAsBoolean(PROXY_ENABLED_VAL)) {
            System.out.println("Windows Proxy Server is enabled");

            final String autoConfigUrlValue = queryResult.getValue(AUTO_CONFIG_URL_VAL);
            if (autoConfigUrlValue != null) {
                System.out.println("Windows Proxy Server use PAC with URL: " + autoConfigUrlValue);
            } else {
                final String proxyServerValue = queryResult.getValue(PROXY_SERVER_REGISTRY_VAL);
                if (proxyServerValue != null) {
                    Arrays.asList(proxyServerValue.split(Pattern.quote(";"))).forEach(p -> System.out.println("Proxy: " + p));
                } else {
                    System.out.println("No specific Proxy server defined. DIRECT will be used");
                }
                final String overrideHostsValue = queryResult.getValue(PROXY_SERVER_OVERRIDE_VAL);
                if (overrideHostsValue != null) {
                    Arrays.asList(overrideHostsValue.split(Pattern.quote(";"))).forEach(p -> System.out.println("Exclusion: " + p));
                } else {
                    System.out.println("No exclusion defined");
                }
            }
        } else {
            System.out.println("Windows Proxy Server is not enabled");
        }
    }

    public static void main(String[] args) throws Exception {
        printProxySettings();
    }
}
