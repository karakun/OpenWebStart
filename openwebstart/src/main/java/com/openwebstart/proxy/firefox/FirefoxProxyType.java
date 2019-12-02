package com.openwebstart.proxy.firefox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * See the following resources for the values assigned to the enums:
 * http://kb.mozillazine.org/Network.proxy.type
 * https://bugzilla.mozilla.org/show_bug.cgi?id=115720
 * https://bugzilla.mozilla.org/show_bug.cgi?id=500983
 * https://bugzilla.mozilla.org/attachment.cgi?id=392739&action=diff
 */
public enum FirefoxProxyType {

    BROWSER_PROXY_TYPE_NONE(0, 3),
    BROWSER_PROXY_TYPE_MANUAL(1),
    BROWSER_PROXY_TYPE_PAC(2),
    BROWSER_PROXY_TYPE_AUTO(4),
    BROWSER_PROXY_TYPE_SYSTEM(5);

    public int getConfigValue() {
        return configValue.get(0);
    }

    private final List<Integer> configValue = new ArrayList<>();

    FirefoxProxyType(final int main, final int... alt) {
        configValue.add(main);
        for (int i : alt) {
            configValue.add(i);
        }
    }

    public static FirefoxProxyType getForConfigValue(final int value) {
        return Stream.of(FirefoxProxyType.values())
                .filter(t -> t.configValue.contains(value))
                .findFirst()
                .orElse(BROWSER_PROXY_TYPE_SYSTEM);
    }
}
