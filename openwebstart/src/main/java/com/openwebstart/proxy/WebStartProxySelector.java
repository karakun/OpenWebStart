package com.openwebstart.proxy;

import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class WebStartProxySelector extends ProxySelector {

    private static final Logger LOG = LoggerFactory.getLogger(WebStartProxySelector.class);

    private final ProxyProvider proxyProvider;

    public WebStartProxySelector(final DeploymentConfiguration config) {
        this.proxyProvider = createProvider(config);
    }

    private ProxyProvider createProvider(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");

        try {
            final String proxyTypeString = config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE);
            final int proxyTypeConfigValue = Integer.parseInt(proxyTypeString);
            final ProxyProviderType providerType = ProxyProviderType.getForConfigValue(proxyTypeConfigValue);

            providerType.checkSupported();
            LOG.debug("Selected ProxyProvider : {} ", providerType);
            return providerType.createProvider(config);
        } catch (final Exception e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("proxy.error.creationFailed"), e);
            return JNLPRuntime.exit(-1);
        }
    }

    @Override
    public List<Proxy> select(final URI uri) {
        try {
            return proxyProvider.select(uri);
        } catch (final Exception e) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("proxy.error.selectionFailed", uri), e);
            return JNLPRuntime.exit(-1);
        }
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress proxyAddress, final IOException ioe) {
        final String currentProxyAddress = addressString(proxyAddress);
        final List<String> proxyAddresses = select(uri).stream()
                .map(Proxy::address)
                .map(this::addressString)
                .collect(Collectors.toList());

        LOG.debug("Connection failed for proxy {} out of  {}", currentProxyAddress, proxyAddresses);

        // if the failed proxy is the last in the list that means all proxies in the list have failed
        if (proxyAddresses.get(proxyAddresses.size() - 1).equals(currentProxyAddress)) {
            DialogFactory.showErrorDialog(Translator.getInstance().translate("proxy.error.connectionFailed", currentProxyAddress, uri), ioe);
            JNLPRuntime.exit(-1);
        }
    }

    private String addressString(SocketAddress proxyAddress) {
        return proxyAddress != null ? proxyAddress.toString() : "DIRECT";
    }
}
