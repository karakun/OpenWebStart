package com.openwebstart.proxy.pac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.List;

class PacBasedProxyProviderTest {

    @Test
    void selectTest() throws Exception {
        //given
        final URL pacUrl = PacBasedProxyProviderTest.class.getResource("simple-pac.js");
        final URI uri = new URI("http://anyserver:8080");
        final PacBasedProxyProvider pacBasedProxyProvider = new PacBasedProxyProvider(pacUrl, new NoopPacProxyCache());

        //when
        final List<Proxy> proxies = pacBasedProxyProvider.select(uri);

        //than
        Assertions.assertNotNull(proxies);
        Assertions.assertEquals(2, proxies.size());
        Assertions.assertEquals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)), proxies.get(0));
        Assertions.assertEquals(Proxy.NO_PROXY, proxies.get(1));
    }

    @Test
    void selectTest2() throws Exception {
        //given
        final URL pacUrl = PacBasedProxyProviderTest.class.getResource("simple-pac.js");
        final URI uri = new URI("http://myserver:8080");
        final PacBasedProxyProvider pacBasedProxyProvider = new PacBasedProxyProvider(pacUrl, new NoopPacProxyCache());

        //when
        final List<Proxy> proxies = pacBasedProxyProvider.select(uri);

        //than
        Assertions.assertNotNull(proxies);
        Assertions.assertEquals(1, proxies.size());
        Assertions.assertEquals(Proxy.NO_PROXY, proxies.get(0));
    }

    @Test
    void selectTest3() throws Exception {
        //given
        final URL pacUrl = PacBasedProxyProviderTest.class.getResource("simple-pac.js");
        final URI uri = new URI("http://noproxy:8080");
        final PacBasedProxyProvider pacBasedProxyProvider = new PacBasedProxyProvider(pacUrl, new NoopPacProxyCache());

        //when
        final List<Proxy> proxies = pacBasedProxyProvider.select(uri);

        //than
        Assertions.assertNotNull(proxies);
        Assertions.assertEquals(1, proxies.size());
        Assertions.assertEquals(Proxy.NO_PROXY, proxies.get(0));
    }
}