package com.openwebstart.proxy.pac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

class PacFileEvaluatorTest {

    @Test
    public void testSimplePac() throws IOException {
        //given
        final URL url = new URL("http://anyserver:8080");
        final PacFileEvaluator evaluator = new PacFileEvaluator(PacFileEvaluatorTest.class.getResource("simple-pac.js"));

        //when
        final String proxy = evaluator.getProxies(url);

        //than
        Assertions.assertNotNull(proxy);
        Assertions.assertEquals("PROXY proxy.example.com:8080; DIRECT", proxy);
    }

    @Test
    public void testSimplePac2() throws IOException {
        //given
        final URL url = new URL("http://myserver:8080");
        final PacFileEvaluator evaluator = new PacFileEvaluator(PacFileEvaluatorTest.class.getResource("simple-pac.js"));

        //when
        final String proxy = evaluator.getProxies(url);

        //than
        Assertions.assertNotNull(proxy);
        Assertions.assertEquals("DIRECT", proxy);
    }

}