package com.openwebstart.proxy.windows;

public interface WindowsProxyConstants {

    String PROXY_REGISTRY_KEY = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

    String PROXY_SERVER_REGISTRY_VAL = "ProxyServer";

    String PROXY_SERVER_OVERRIDE_VAL = "ProxyOverride";

    String PROXY_ENABLED_VAL = "ProxyEnable";

    String AUTO_CONFIG_URL_VAL = "AutoConfigURL";
}
