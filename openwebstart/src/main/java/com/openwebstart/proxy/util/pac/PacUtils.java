package com.openwebstart.proxy.util.pac;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.openwebstart.proxy.util.pac.PacConstants.DIRECT;
import static com.openwebstart.proxy.util.pac.PacConstants.PROXY;
import static com.openwebstart.proxy.util.pac.PacConstants.SOCKS;

//TODO: Class should be refactored
class PacUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PacUtils.class);
    private static final int PREFIX_LENGTH = PROXY.length();

    /**
     * Converts a proxy string from a firefox into a List of Proxy objects
     * suitable for java.
     *
     * @param pacString a string indicating proxies. For example
     *                  "PROXY foo.bar:3128; DIRECT"
     * @return a list of Proxy objects representing the parsed string. In
     * case of malformed input, an empty list may be returned
     */
    static List<Proxy> getProxiesFromPacResult(String pacString) {
        final List<Proxy> proxies = new ArrayList<>();

        final String[] tokens = pacString.split(";");
        for (String token : tokens) {
            if (token.startsWith(PROXY)) {
                getInetSocketAddress(token)
                        .map(sa -> new Proxy(Proxy.Type.HTTP, sa))
                        .ifPresent(proxies::add);
            } else if (token.startsWith(SOCKS)) {
                getInetSocketAddress(token)
                        .map(sa -> new Proxy(Proxy.Type.SOCKS, sa))
                        .ifPresent(proxies::add);
            } else if (token.startsWith(DIRECT)) {
                proxies.add(Proxy.NO_PROXY);
            } else {
                LOG.debug("Unrecognized proxy token: {}", token);
            }
        }

        return proxies;
    }

    private static Optional<InetSocketAddress> getInetSocketAddress(String token) {
        final String hostPortPairString = token.substring(PREFIX_LENGTH).trim();
        final String[] hostPortPair = hostPortPairString.split(":");
        if (hostPortPair.length == 2) {
            try {
                final String host = hostPortPair[0];
                final int port = Integer.parseInt(hostPortPair[1]);
                return Optional.of(new InetSocketAddress(host, port));
            } catch (NumberFormatException ignored) {
            }
        }
        return Optional.empty();
    }

}
