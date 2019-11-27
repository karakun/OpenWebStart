package com.openwebstart.proxy;

import com.openwebstart.proxy.config.ConfigBasedAutoConfigUrlProxyProvider;
import com.openwebstart.proxy.config.ConfigBasedProxyProvider;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.firefox.FirefoxProxyProvider;
import com.openwebstart.proxy.ui.error.ConnectionFailedDialog;
import com.openwebstart.proxy.ui.error.ProxyCreationFailedDialog;
import com.openwebstart.proxy.ui.error.ProxyDialogResult;
import com.openwebstart.proxy.ui.error.ProxyErrorDialog;
import com.openwebstart.proxy.ui.error.ProxySelectionFailedDialog;
import com.openwebstart.proxy.windows.WindowsProxyProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebStartProxySelector extends ProxySelector {

    private final static Logger LOG = LoggerFactory.getLogger(WebStartProxySelector.class);

    private final ProxyProvider proxyProvider;

    private final AtomicBoolean useDirectAfterError;

    public WebStartProxySelector(final DeploymentConfiguration config) throws Exception {
        this.useDirectAfterError = new AtomicBoolean(false);
        this.proxyProvider = createProvider(config);
    }

    private ProxyProvider createProvider(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");

        try {
            final String proxyTypeString = config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE);
            final int proxyTypeConfigValue = Integer.valueOf(proxyTypeString);
            final ProxyProviderTypes providerType = ProxyProviderTypes.getForConfigValue(proxyTypeConfigValue);

            providerType.checkSupported();

            if (providerType == ProxyProviderTypes.NONE) {
                return DirectProxyProvider.getInstance();
            }
            if (providerType == ProxyProviderTypes.MANUAL_HOSTS) {
                return new ConfigBasedProxyProvider(config);
            }
            if (providerType == ProxyProviderTypes.MANUAL_PAC_URL) {
                return new ConfigBasedAutoConfigUrlProxyProvider(config);
            }
            if (providerType == ProxyProviderTypes.FIREFOX) {
                return new FirefoxProxyProvider();
            }
            if (providerType == ProxyProviderTypes.WINDOWS) {
                return new WindowsProxyProvider();
            }
            throw new IllegalStateException("Proxy can not be defined");
        } catch (final Exception e) {
            LOG.error("Error in proxy creation", e);
            final ProxyErrorDialog dialog = new ProxyCreationFailedDialog();
            final ProxyDialogResult proxyDialogResult = dialog.showAndWait();
            if (proxyDialogResult == ProxyDialogResult.EXIT) {
                System.exit(0);
            }
            return DirectProxyProvider.getInstance();
        }
    }

    @Override
    public List<Proxy> select(final URI uri) {
        if (useDirectAfterError.get()) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
        try {
            return proxyProvider.select(uri);
        } catch (final Exception e) {
            LOG.error("Error in proxy selection", e);
            final ProxyErrorDialog dialog = new ProxySelectionFailedDialog();
            final ProxyDialogResult proxyDialogResult = dialog.showAndWait();
            if (proxyDialogResult == ProxyDialogResult.EXIT) {
                System.exit(0);
            } else {
                useDirectAfterError.set(true);
            }
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        LOG.error("Error in proxy connection", ioe);
        final ProxyErrorDialog dialog = new ConnectionFailedDialog();
        final ProxyDialogResult proxyDialogResult = dialog.showAndWait();
        if (proxyDialogResult == ProxyDialogResult.EXIT) {
            System.exit(0);
        } else {
            useDirectAfterError.set(true);
        }
    }
}
