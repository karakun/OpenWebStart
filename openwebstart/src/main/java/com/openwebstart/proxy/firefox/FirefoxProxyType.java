package com.openwebstart.proxy.firefox;

import java.util.stream.Stream;

public enum FirefoxProxyType {

    BROWSER_PROXY_TYPE_NONE(0),
    BROWSER_PROXY_TYPE_MANUAL(1),
    BROWSER_PROXY_TYPE_PAC(2),
    BROWSER_PROXY_TYPE_AUTO(4),
    BROWSER_PROXY_TYPE_SYSTEM(5);

    private final int configValue;

    FirefoxProxyType(final int configValue) {
        this.configValue = configValue;
    }

    public static FirefoxProxyType getForConfigValue(final int value) {
        return Stream.of(FirefoxProxyType.values())
                .filter(t -> value == t.configValue)
                .findFirst()
                .orElse(BROWSER_PROXY_TYPE_NONE);
    }
}
