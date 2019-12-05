package com.openwebstart.proxy.windows;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.proxy.config.ConfigBasedProvider;
import com.openwebstart.proxy.config.ProxyConfigurationImpl;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.pac.PacBasedProxyProvider;

import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.proxy.windows.WindowsProxyConstants.AUTO_CONFIG_URL_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_ENABLED_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_REGISTRY_KEY;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_OVERRIDE_VAL;
import static com.openwebstart.proxy.windows.WindowsProxyConstants.PROXY_SERVER_REGISTRY_VAL;

public class WindowsProxyProvider implements ProxyProvider {

    private final ProxyProvider internalProvider;

    public WindowsProxyProvider() throws Exception {
        final Map<String, RegistryValue> proxyRegistryEntries = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY);
        final RegistryValue proxyEnabledValue = proxyRegistryEntries.get(PROXY_ENABLED_VAL);
        if (proxyEnabledValue != null && proxyEnabledValue.getValueAsBoolean()) {
            final RegistryValue autoConfigUrlValue = proxyRegistryEntries.get(AUTO_CONFIG_URL_VAL);
            if (autoConfigUrlValue != null) {
                final String autoConfigUrl = autoConfigUrlValue.getValue();
                internalProvider = new PacBasedProxyProvider(new URL(autoConfigUrl));
            } else {
                final RegistryValue proxyServerValue = proxyRegistryEntries.get(PROXY_SERVER_REGISTRY_VAL);
                if (proxyServerValue != null) {
                    final ProxyConfigurationImpl proxyConfiguration = new ProxyConfigurationImpl();
                    final List<String> hosts = Arrays.asList(proxyServerValue.getValue().split(Pattern.quote(";")));
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
                    final RegistryValue overrideHostsValue = proxyRegistryEntries.get(PROXY_SERVER_OVERRIDE_VAL);
                    if (overrideHostsValue != null) {
                        Arrays.asList(overrideHostsValue.getValue().split(Pattern.quote(";"))).forEach(p -> proxyConfiguration.addToBypassList(p));
                    }
                    internalProvider = new ConfigBasedProvider(proxyConfiguration);
                } else {
                    //TODO: is this correct?
                    internalProvider = DirectProxyProvider.getInstance();
                }
            }
        } else {
            internalProvider = DirectProxyProvider.getInstance();
        }
    }

    private void findProxyForProtocol(final List<String> hosts, final String protocol, final BiConsumer<String, Integer> consumer) {
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

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
