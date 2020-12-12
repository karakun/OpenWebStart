package com.openwebstart.proxy.config;

import com.openwebstart.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.IpUtil;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.openwebstart.proxy.util.CidrUtils.isCidrNotation;
import static com.openwebstart.proxy.util.CidrUtils.isInRange;
import static com.openwebstart.proxy.util.CidrUtils.isIpv4;
import static com.openwebstart.proxy.util.ProxyConstants.FTP_SCHEMA;
import static com.openwebstart.proxy.util.ProxyConstants.HTTPS_SCHEMA;
import static com.openwebstart.proxy.util.ProxyConstants.HTTP_SCHEMA;
import static com.openwebstart.proxy.util.ProxyConstants.SOCKET_SCHEMA;


public class ConfigBasedProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigBasedProvider.class);

    private final ProxyConfiguration configuration;

    public ConfigBasedProvider(final ProxyConfiguration proxyConfiguration) {
        this.configuration = Assert.requireNonNull(proxyConfiguration, "proxyConfiguration");

        logConfig();
    }

    @Override
    public List<Proxy> select(final URI uri) {
        Assert.requireNonNull(uri, "uri");

        if (configuration.isBypassLocal() && IpUtil.isLocalhostOrLoopback(uri)) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }

        if (isExcluded(uri)) {
            LOG.debug("URL {} is excluded", uri);
            return Collections.singletonList(Proxy.NO_PROXY);
        }

        final List<Proxy> proxies = new ArrayList<>();
        final String scheme = uri.getScheme();

        if (configuration.isUseHttpForHttpsAndFtp()) {
            configuration.getHttpAddress().ifPresent(httpAddress -> {
                if ((scheme.equals(HTTPS_SCHEMA) || scheme.equals(HTTP_SCHEMA) || scheme.equals(FTP_SCHEMA))) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, httpAddress);
                    proxies.add(proxy);
                }
                if (scheme.equals(SOCKET_SCHEMA) && configuration.isUseHttpForSocks()) {
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, httpAddress);
                    proxies.add(proxy);
                } else {
                    configuration.getSocksAddress().ifPresent(socksAddress -> proxies.add(new Proxy(Proxy.Type.SOCKS, socksAddress)));
                }
            });
        } else if (scheme.equals(HTTP_SCHEMA)) {
            configuration.getHttpAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(HTTPS_SCHEMA)) {
            configuration.getHttpsAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(FTP_SCHEMA)) {
            configuration.getFtpAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(SOCKET_SCHEMA)) {
            configuration.getSocksAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.SOCKS, address)));
        }

        if (proxies.isEmpty()) {
            LOG.debug("No proxy found for '{}'. Falling back to NO_PROXY", uri);
            proxies.add(Proxy.NO_PROXY);
        } else {
            LOG.debug("Proxies found for '{}' : {}", uri, proxies);
        }
        return proxies;
    }

    private boolean isExcluded(final URI uri) {
        return configuration.getBypassList().stream()
                .anyMatch(exclusion -> isExcluded(uri, exclusion));
    }

    private boolean isExcluded(URI uri, String exclusion) {
        final String host = uri.getHost();

        // google.de
        if (Objects.equals(host, exclusion)) {
            return true;
        }

        // *.local
        if (exclusion.startsWith("*.") && host.endsWith(exclusion.substring(1))) {
            return true;
        }

        // .mozilla
        if (exclusion.startsWith(".") && host.endsWith(exclusion)) {
            return true;
        }

        try {
            final InetSocketAddress socketAddress = new InetSocketAddress(host, 0);
            final String ipAddress = socketAddress.getAddress().getHostAddress();
            // 169.254.120.4
            if (Objects.equals(ipAddress, exclusion)) {
                return true;
            }

            // 169.254/16
            if (isCidrNotation(exclusion) && isIpv4(ipAddress)) {
                return isInRange(exclusion, ipAddress);
            }
        } catch (final Exception e) {
            LOG.debug("Looks like we cannot get the socket address for '{}'. error: '{}'", uri, e.getMessage());
        }

        return false;
    }

    private void logConfig() {
        LOG.debug("Http proxy congfig: host {} - port {}", configuration.getHttpHost(), configuration.getHttpPort());
        LOG.debug("Https proxy congfig: host {} - port {}", configuration.getHttpsHost(), configuration.getHttpsPort());
        LOG.debug("Ftp proxy congfig: host {} - port {}", configuration.getFtpHost(), configuration.getFtpPort());
        LOG.debug("Socks proxy congfig: host {} - port {}", configuration.getSocksHost(), configuration.getSocksPort());
        LOG.debug("proxy bypass list: {}", configuration.getBypassList());
    }
}
