package com.openwebstart.proxy.config;

import com.openwebstart.proxy.util.ProxyUtlis;

import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;


public interface ProxyConfiguration {

    String getHttpHost();

    int getHttpPort();

    default Optional<SocketAddress> getHttpAddress() {
        return ProxyUtlis.getAddress(getHttpHost(), getHttpPort());
    }

    String getHttpsHost();

    int getHttpsPort();

    default Optional<SocketAddress> getHttpsAddress() {
        return ProxyUtlis.getAddress(getHttpsHost(), getHttpsPort());
    }

    String getFtpHost();

    int getFtpPort();

    default Optional<SocketAddress> getFtpAddress() {
        return ProxyUtlis.getAddress(getFtpHost(), getFtpPort());
    }

    String getSocksHost();

    int getSocksPort();

    default Optional<SocketAddress> getSocksAddress() {
        return ProxyUtlis.getAddress(getSocksHost(), getSocksPort());
    }

    List<String> getBypassList();

    boolean isBypassLocal();

    boolean isUseHttpForHttpsAndFtp();

    boolean isUseHttpForSocks();

}
