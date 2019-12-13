package com.openwebstart.proxy.pac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacUtilsTest {

    @Test
    void getProxiesFromPacResultTest() {
        //given
        final String pacResult = "PROXY proxy.example.com:8080";

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assertions.assertNotNull(proxiesFromPacResult);
        Assertions.assertEquals(1, proxiesFromPacResult.size());
        Assertions.assertEquals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)), proxiesFromPacResult.get(0));
    }

    @Test
    void getProxiesFromPacResultTest2() {
        //given
        final String pacResult = "DIRECT";

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assertions.assertNotNull(proxiesFromPacResult);
        Assertions.assertEquals(1, proxiesFromPacResult.size());
        Assertions.assertEquals(Proxy.NO_PROXY, proxiesFromPacResult.get(0));
    }

    @Test
    void getProxiesFromPacResultTest3() {
        //given
        final String pacResult = "PROXY proxy.example.com:8080; DIRECT";

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assertions.assertNotNull(proxiesFromPacResult);
        Assertions.assertEquals(2, proxiesFromPacResult.size());
        Assertions.assertEquals(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)), proxiesFromPacResult.get(0));
        Assertions.assertEquals(Proxy.NO_PROXY, proxiesFromPacResult.get(1));

    }

    @Test
    void getProxiesFromPacResultTest4() {
        //given
        final String pacResult = null;

        //when
        final List<Proxy> proxiesFromPacResult = PacUtils.getProxiesFromPacResult(pacResult);

        //than
        Assertions.assertNotNull(proxiesFromPacResult);
        Assertions.assertEquals(1, proxiesFromPacResult.size());
        Assertions.assertEquals(Proxy.NO_PROXY, proxiesFromPacResult.get(0));

    }
}