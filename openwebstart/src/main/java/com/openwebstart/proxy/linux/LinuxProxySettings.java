package com.openwebstart.proxy.linux;

import com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinuxProxySettings {

    private String httpHost;

    private int httpPort;

    private String httpsHost;

    private int httpsPort;

    private boolean httpEnabled;

    private boolean httpsEnabled;

    private boolean ftpEnabled;

    private String ftpHost;

    private int ftpPort;

    private boolean socksEnabled;

    private String socksHost;

    private int socksPort;

    private boolean autoConfigEnabled;

    private URL autoConfigUrl;

    private boolean autoDiscoveryEnabled;

    private boolean excludeSimpleHostnames;

    private boolean ftpPassive;

    private String httpUser;

    private List<String> exceptionList = new ArrayList<>();
    private String httpsUser;
    private String ftpUser;
    private String socksUser;
    private LinuxProxyMode mode;
    private boolean excludeLocalhost;
    private boolean authenticationEnabled;

    public void setHttpHost(final String httpHost) {
        this.httpHost = httpHost;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpsHost(final String httpsHost) {
        this.httpsHost = httpsHost;
    }

    public String getHttpsHost() {
        return httpsHost;
    }

    public void setHttpsPort(final int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpEnabled(final boolean httpEnabled) {
        this.httpEnabled = httpEnabled;
    }

    public boolean isHttpEnabled() {
        return httpEnabled;
    }

    public void setHttpsEnabled(final boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public void setFtpEnabled(final boolean ftpEnabled) {
        this.ftpEnabled = ftpEnabled;
    }

    public boolean isFtpEnabled() {
        return ftpEnabled;
    }

    public void setFtpHost(final String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public void setFtpPort(final int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setSocksEnabled(final boolean socksEnabled) {
        this.socksEnabled = socksEnabled;
    }

    public boolean isSocksEnabled() {
        return socksEnabled;
    }

    public void setSocksHost(final String socksHost) {
        this.socksHost = socksHost;
    }

    public String getSocksHost() {
        return socksHost;
    }

    public void setSocksPort(final int socksPort) {
        this.socksPort = socksPort;
    }

    public int getSocksPort() {
        return socksPort;
    }

    public void setAutoConfigUrl(final URL autoConfigUrl) {
        this.autoConfigUrl = autoConfigUrl;
    }

    public URL getAutoConfigUrl() {
        return autoConfigUrl;
    }

    public void setExceptionsList(final List<String> exceptionList) {
        this.exceptionList = new ArrayList<>(exceptionList);
    }

    public List<String> getExceptionList() {
        return Collections.unmodifiableList(exceptionList);
    }


    public void setMode(LinuxProxyMode mode) {
        this.mode = mode;
    }

    public LinuxProxyMode getMode() {
        return mode;
    }

    public void setLocalhostExcluded(boolean excludeLocalhost) {
        this.excludeLocalhost = excludeLocalhost;
    }

    public boolean isLocalhostExcluded() {
        return excludeLocalhost;
    }

    public void setAuthenticationEnabled(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }
}
