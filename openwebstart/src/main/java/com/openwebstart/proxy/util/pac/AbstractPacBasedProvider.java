package com.openwebstart.proxy.util.pac;

import com.openwebstart.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.Assert;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public abstract class AbstractPacBasedProvider implements ProxyProvider {

    protected abstract PacFileEvaluator getPacEvaluator();

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        Assert.requireNonNull(uri, "uri");
        final PacFileEvaluator pacEvaluator = getPacEvaluator();
        Assert.requireNonNull(pacEvaluator, "pacEvaluator");
        final String proxiesString = pacEvaluator.getProxies(uri.toURL());
        final List<Proxy> proxies = PacUtils.getProxiesFromPacResult(proxiesString);
        return Collections.unmodifiableList(proxies);
    }

}
