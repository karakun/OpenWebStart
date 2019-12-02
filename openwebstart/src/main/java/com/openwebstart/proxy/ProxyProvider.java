package com.openwebstart.proxy;

import java.net.Proxy;
import java.net.URI;
import java.util.List;

public interface ProxyProvider {

    List<Proxy> select(final URI uri) throws Exception;

}
