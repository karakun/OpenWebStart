package com.openwebstart.proxy.pac;

import com.openwebstart.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class PacBasedProxyProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PacBasedProxyProvider.class);

    private final PacFileEvaluator pacEvaluator;

    public PacBasedProxyProvider(final URL pacConfigFileUrl) throws IOException {
        Assert.requireNonNull(pacConfigFileUrl, "pacConfigFileUrl");
        this.pacEvaluator = new PacFileEvaluator(pacConfigFileUrl);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        Assert.requireNonNull(uri, "uri");

        final String proxiesString = pacEvaluator.getProxies(uri.toURL());
        final List<Proxy> proxies = PacUtils.getProxiesFromPacResult(proxiesString);
        LOG.debug("PAC Proxies found for '{}' : {}", uri, proxies);
        return Collections.unmodifiableList(proxies);
    }
}
