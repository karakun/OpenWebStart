package com.openwebstart.proxy;

import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.ui.error.ConnectionFailedDialog;
import com.openwebstart.proxy.ui.error.ProxyCreationFailedDialog;
import com.openwebstart.proxy.ui.error.ProxyDialogResult;
import com.openwebstart.proxy.ui.error.ProxyErrorDialog;
import com.openwebstart.proxy.ui.error.ProxySelectionFailedDialog;
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

    private static final Logger LOG = LoggerFactory.getLogger(WebStartProxySelector.class);

    private final ProxyProvider proxyProvider;

    private final AtomicBoolean useDirectAfterError;

    public WebStartProxySelector(final DeploymentConfiguration config) {
        this.useDirectAfterError = new AtomicBoolean(false);
        this.proxyProvider = createProvider(config);
    }

    private ProxyProvider createProvider(final DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");

        try {
            final String proxyTypeString = config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE);
            final int proxyTypeConfigValue = Integer.parseInt(proxyTypeString);
            final ProxyProviderType providerType = ProxyProviderType.getForConfigValue(proxyTypeConfigValue);

            providerType.checkSupported();
            return providerType.createProvider(config);
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
