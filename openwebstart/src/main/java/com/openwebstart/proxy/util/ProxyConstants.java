package com.openwebstart.proxy.util;

public interface ProxyConstants {

    String HTTP_SCHEMA = "http";

    String HTTPS_SCHEMA = "https";

    String FTP_SCHEMA = "ftp";

    String SOCKET_SCHEMA = "socket";

    /**
     * The default port to use as a fallback. Currently squid's default port
     */
    int FALLBACK_PROXY_PORT = 3128;
}
