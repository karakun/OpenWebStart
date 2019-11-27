package com.openwebstart.proxy.util.pac;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static com.openwebstart.proxy.util.pac.PacConstants.DIRECT;
import static com.openwebstart.proxy.util.pac.PacConstants.PROXY;
import static com.openwebstart.proxy.util.pac.PacConstants.SOCKS;


public class PacUtils {

    private final static Logger LOG = LoggerFactory.getLogger(PacUtils.class);

    /**
     * Converts a proxy string from a firefox into a List of Proxy objects
     * suitable for java.
     *
     * @param pacString a string indicating proxies. For example
     *                  "PROXY foo.bar:3128; DIRECT"
     * @return a list of Proxy objects representing the parsed string. In
     * case of malformed input, an empty list may be returned
     */
    public static List<Proxy> getProxiesFromPacResult(String pacString) {
        List<Proxy> proxies = new ArrayList<>();

        String[] tokens = pacString.split(";");
        for (String token : tokens) {
            if (token.startsWith(PROXY)) {
                String hostPortPair = token.substring(PROXY.length()).trim();
                if (!hostPortPair.contains(":")) {
                    continue;
                }
                String host = hostPortPair.split(":")[0];
                int port;
                try {
                    port = Integer.valueOf(hostPortPair.split(":")[1]);
                } catch (NumberFormatException nfe) {
                    continue;
                }
                SocketAddress sa = new InetSocketAddress(host, port);
                proxies.add(new Proxy(Proxy.Type.HTTP, sa));
            } else if (token.startsWith(SOCKS)) {
                String hostPortPair = token.substring(SOCKS.length()).trim();
                if (!hostPortPair.contains(":")) {
                    continue;
                }
                String host = hostPortPair.split(":")[0];
                int port;
                try {
                    port = Integer.valueOf(hostPortPair.split(":")[1]);
                } catch (NumberFormatException nfe) {
                    continue;
                }
                SocketAddress sa = new InetSocketAddress(host, port);
                proxies.add(new Proxy(Proxy.Type.SOCKS, sa));
            } else if (token.startsWith(DIRECT)) {
                proxies.add(Proxy.NO_PROXY);
            } else {
                LOG.debug("Unrecognized proxy token: {}", token);
            }
        }

        return proxies;
    }
}
