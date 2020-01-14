package com.openwebstart.proxy.windows;

public interface WindowsProxyConstants {

    String PROXY_REGISTRY_KEY = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

    String PROXY_SERVER_REGISTRY_VAL = "ProxyServer";

    String PROXY_SERVER_OVERRIDE_VAL = "ProxyOverride";

    String PROXY_ENABLED_VAL = "ProxyEnable";

    String AUTO_CONFIG_URL_VAL = "AutoConfigURL";

    // see https://blogs.msdn.microsoft.com/askie/2015/10/12/how-to-configure-proxy-settings-for-ie10-and-ie11-as-iem-is-not-available/
    String EXCLUDE_LOCALHOST_MAGIC_VALUE = "<local>";
}
