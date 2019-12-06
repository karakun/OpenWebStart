package com.openwebstart.proxy.linux;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.MANUAL;
import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.NO_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.PROPERTY_FTP_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.PROPERTY_HTTPS_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.PROPERTY_HTTP_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.PROPERTY_NO_PROXY;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

class SystemPropertiesProxyConfigReader {

    private static final Logger LOG = LoggerFactory.getLogger(SystemPropertiesProxyConfigReader.class);

    private static final Set<String> MAGIC_LOCALHOST_VALUES = new HashSet<>(asList("localhost", "127.0.0.1", "127.0.0.0/8"));

    public static Optional<LinuxProxySettings> readSystemPropertiesProxyConfig() {

        try {
            final Properties properties = System.getProperties();
            final LinuxProxySettings result = new LinuxProxySettings();
            result.setMode(NO_PROXY);

            for (String propertyName : properties.stringPropertyNames()) {
                if (PROPERTY_HTTP_PROXY.equalsIgnoreCase(propertyName)) {
                    result.setMode(MANUAL);
                    final HostAndPort hostAndPort = parseHostAndPort(properties.getProperty(propertyName));
                    result.setHttpHost(hostAndPort.host);
                    result.setHttpPort(hostAndPort.port);
                } else if (PROPERTY_HTTPS_PROXY.equalsIgnoreCase(propertyName)) {
                    result.setMode(MANUAL);
                    final HostAndPort hostAndPort = parseHostAndPort(properties.getProperty(propertyName));
                    result.setHttpsHost(hostAndPort.host);
                    result.setHttpsPort(hostAndPort.port);
                } else if (PROPERTY_FTP_PROXY.equalsIgnoreCase(propertyName)) {
                    result.setMode(MANUAL);
                    final HostAndPort hostAndPort = parseHostAndPort(properties.getProperty(propertyName));
                    result.setFtpHost(hostAndPort.host);
                    result.setFtpPort(hostAndPort.port);
                } else if (PROPERTY_NO_PROXY.equalsIgnoreCase(propertyName)) {
                    final List<String> exceptionList = asList(properties.getProperty(propertyName).split(","));
                    result.setExceptionsList(exceptionList);
                    result.setLocalhostExcluded(containsLocalhost(exceptionList));
                }
            }

            return Optional.of(result);
        } catch (Exception e) {
            LOG.debug("Could not load system properties proxy settings {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static HostAndPort parseHostAndPort(String value) {
        final String[] parts = value.split(":");
        return new HostAndPort(parts[0], extractPort(parts));
    }

    private static int extractPort(String[] parts) {
        try {
            return parseInt(parts[1]);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static boolean containsLocalhost(List<String> exceptionList) {
        return exceptionList.stream().anyMatch(MAGIC_LOCALHOST_VALUES::contains);
    }

    private static class HostAndPort {
        final String host;
        final int port;

        private HostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
