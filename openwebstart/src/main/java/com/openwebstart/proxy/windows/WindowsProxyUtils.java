package com.openwebstart.proxy.windows;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.config.ConfigBasedProvider;
import com.openwebstart.proxy.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.pac.PacBasedProxyProvider;
import com.openwebstart.proxy.pac.PacProxyCache;
import com.openwebstart.proxy.windows.registry.RegistryQueryResult;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.proxy.windows.WindowsProxyConstants.AUTO_CONFIG_URL_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.EXCLUDE_LOCALHOST_MAGIC_VALUE;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_ENABLED_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_OVERRIDE_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_REGISTRY_VAL;

class WindowsProxyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WindowsProxyUtils.class);

    static ProxyProvider createInternalProxy(final DeploymentConfiguration config, final RegistryQueryResult queryResult) throws IOException {
        final String autoConfigUrl = queryResult.getValue(AUTO_CONFIG_URL_VAL);
        if (autoConfigUrl != null) {
            LOG.debug("Registry value '{}' specified. Will use pac based proxy with pac url '{}'.", AUTO_CONFIG_URL_VAL, autoConfigUrl);
            return new PacBasedProxyProvider(new URL(autoConfigUrl), PacProxyCache.createFor(config));
        } else {
            final boolean proxyEnabledValue = queryResult.getValueAsBoolean(PROXY_ENABLED_VAL);
            if (proxyEnabledValue) {
                final String proxyServerValue = queryResult.getValue(PROXY_SERVER_REGISTRY_VAL);
                if (proxyServerValue != null) {
                    LOG.debug("Proxy server(s) defined ( registry value '" + PROXY_SERVER_REGISTRY_VAL + "'). Will use configured proxy.");
                    final ProxyConfigurationImpl proxyConfiguration = getProxyConfiguration(queryResult);
                    return new ConfigBasedProvider(proxyConfiguration);
                } else {
                    //TODO: is this correct?
                    LOG.debug("No proxy server defined ( registry value '" + PROXY_SERVER_REGISTRY_VAL + "'). Will use direct proxy.");
                    return DirectProxyProvider.getInstance();
                }
            } else {
                LOG.debug("Proxy disabled ( registry value '" + PROXY_ENABLED_VAL + "'). Will use direct proxy.");
                return DirectProxyProvider.getInstance();
            }
        }
    }

    private static ProxyConfigurationImpl getProxyConfiguration(final RegistryQueryResult queryResult) {
        final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();

        final List<String> hosts = Optional.ofNullable(queryResult.getValue(PROXY_SERVER_REGISTRY_VAL))
                .map(v -> v.split(Pattern.quote(";")))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());

        if (hosts.size() == 1) {
            //HTTP use for all other
            proxyConfiguration.setUseHttpForHttpsAndFtp(true);
            proxyConfiguration.setUseHttpForSocks(true);

            final String[] split = hosts.get(0).split(Pattern.quote(":"), 2);
            if (split.length == 1) {
                //TODO: Default port??
                throw new IllegalStateException("No port defined!");
            } else {
                //TODO: We need to check port behavior on win
                proxyConfiguration.setHttpHost(split[0]);
                proxyConfiguration.setHttpPort(Integer.parseInt(split[1]));
            }
        } else if (hosts.size() == 0) {
            //TODO: How does windows behave???
            throw new IllegalStateException("No host defined!");
        } else {
            findProxyForProtocol(hosts, "http", (host, port) -> {
                proxyConfiguration.setHttpHost(host);
                proxyConfiguration.setHttpPort(port);
            });
            findProxyForProtocol(hosts, "https", (host, port) -> {
                proxyConfiguration.setHttpsHost(host);
                proxyConfiguration.setHttpsPort(port);
            });
            findProxyForProtocol(hosts, "ftp", (host, port) -> {
                proxyConfiguration.setFtpHost(host);
                proxyConfiguration.setFtpPort(port);
            });
            findProxyForProtocol(hosts, "socks", (host, port) -> {
                proxyConfiguration.setSocksHost(host);
                proxyConfiguration.setSocksPort(port);
            });
        }
        final String overrideHostsValue = queryResult.getValue(PROXY_SERVER_OVERRIDE_VAL);
        if (overrideHostsValue != null) {
            Arrays.asList(overrideHostsValue.split(Pattern.quote(";"))).forEach(p -> proxyConfiguration.addToBypassList(p));
        }
        proxyConfiguration.setBypassLocal(proxyConfiguration.getBypassList().contains(EXCLUDE_LOCALHOST_MAGIC_VALUE));
        return proxyConfiguration;
    }

    private static void findProxyForProtocol(final List<String> hosts, final String protocol, final BiConsumer<String, Integer> consumer) {
        final List<String> httpDefs = hosts.stream()
                .filter(v -> v.startsWith(protocol + "="))
                .map(v -> v.substring(protocol.length() + 1))
                .collect(Collectors.toList());
        if (httpDefs.size() > 1) {
            throw new IllegalStateException("More than 1 proxy defined for " + protocol);
        } else if (httpDefs.size() == 1) {
            final String[] split = httpDefs.get(0).split(Pattern.quote(":"), 2);
            if (split.length == 1) {
                //TODO: Default port??
                throw new IllegalStateException("No port defined!");
            } else {
                //TODO: We need to check port behavior on win
                consumer.accept(split[0], Integer.parseInt(split[1]));
            }
        } else {
            //TODO: LOG
        }
    }
}
