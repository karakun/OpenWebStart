package com.openwebstart.proxy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProxyConfigurationImpl implements ProxyConfiguration {

    private String httpHost;

    private int httpPort;

    private String httpsHost;

    private int httpsPort;

    private String ftpHost;

    private int ftpPort;

    private String socksHost;

    private int socksPort;

    /**
     * a list of URLs that should be bypassed for proxy purposes
     */
    private final List<String> bypassList = new ArrayList<>();

    /**
     * whether localhost should be bypassed for proxy purposes
     */
    private boolean bypassLocal;

    /**
     * whether the http proxy should be used for https and ftp protocols as well
     */
    private boolean useHttpForHttpsAndFtp;

    private boolean useHttpForSocks;

    public void setHttpHost(final String httpHost) {
        this.httpHost = httpHost;
    }

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }

    public void setHttpsHost(final String httpsHost) {
        this.httpsHost = httpsHost;
    }

    public void setHttpsPort(final int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public void setFtpHost(final String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public void setFtpPort(final int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public void setSocksHost(final String socksHost) {
        this.socksHost = socksHost;
    }

    public void setSocksPort(final int socksPort) {
        this.socksPort = socksPort;
    }

    public void setBypassLocal(final boolean bypassLocal) {
        this.bypassLocal = bypassLocal;
    }

    public void setUseHttpForHttpsAndFtp(final boolean useHttpForHttpsAndFtp) {
        this.useHttpForHttpsAndFtp = useHttpForHttpsAndFtp;
    }

    public void addToBypassList(final String url) {
        bypassList.add(url);
    }

    public void setUseHttpForSocks(final boolean useHttpForSocks) {
        this.useHttpForSocks = useHttpForSocks;
    }

    @Override
    public String getHttpHost() {
        return httpHost;
    }

    @Override
    public int getHttpPort() {
        return httpPort;
    }

    @Override
    public String getHttpsHost() {
        return httpsHost;
    }

    @Override
    public int getHttpsPort() {
        return httpsPort;
    }

    @Override
    public String getFtpHost() {
        return ftpHost;
    }

    @Override
    public int getFtpPort() {
        return ftpPort;
    }

    @Override
    public String getSocksHost() {
        return socksHost;
    }

    @Override
    public int getSocksPort() {
        return socksPort;
    }

    @Override
    public List<String> getBypassList() {
        return Collections.unmodifiableList(bypassList);
    }

    @Override
    public boolean isBypassLocal() {
        return bypassLocal;
    }

    @Override
    public boolean isUseHttpForHttpsAndFtp() {
        return useHttpForHttpsAndFtp;
    }

    @Override
    public boolean isUseHttpForSocks() {
        return useHttpForSocks;
    }
}
