package com.openwebstart.proxy.linux;

interface LinuxProxyProviderConstants {

    String GNOME_PROXY_MODE = "org.gnome.system.proxy:mode";
    String GNOME_PROXY_AUTOCONFIG_URL = "org.gnome.system.proxy:autoconfig-url";
    String GNOME_PROXY_IGNORE_HOSTS = "org.gnome.system.proxy:ignore-hosts";
    String GNOME_PROXY_HTTP_HOST = "org.gnome.system.proxy.http:host";
    String GNOME_PROXY_HTTP_PORT = "org.gnome.system.proxy.http:port";
    String GNOME_PROXY_HTTPS_HOST = "org.gnome.system.proxy.https:host";
    String GNOME_PROXY_HTTPS_PORT = "org.gnome.system.proxy.https:port";
    String GNOME_PROXY_FTP_HOST = "org.gnome.system.proxy.ftp:host";
    String GNOME_PROXY_FTP_PORT = "org.gnome.system.proxy.ftp:port";
    String GNOME_PROXY_SOCKS_HOST = "org.gnome.system.proxy.socks:host";
    String GNOME_PROXY_SOCKS_PORT = "org.gnome.system.proxy.socks:port";
    // not supported because only valid fro HTTP - see manual of gsettings
    String GNOME_PROXY_USE_AUTHENTICATION = "org.gnome.system.proxy.http:use-authentication";

    String PROPERTY_HTTP_PROXY = "http_proxy";
    String PROPERTY_HTTPS_PROXY = "https_proxy";
    String PROPERTY_FTP_PROXY = "ftp_proxy";
    String PROPERTY_NO_PROXY = "no_proxy";
}
