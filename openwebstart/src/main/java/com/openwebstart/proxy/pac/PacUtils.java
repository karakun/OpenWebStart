package com.openwebstart.proxy.pac;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.openwebstart.proxy.pac.PacConstants.DIRECT;
import static com.openwebstart.proxy.pac.PacConstants.PROXY;
import static com.openwebstart.proxy.pac.PacConstants.SOCKS;

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
    static List<Proxy> getProxiesFromPacResult(final String pacString) {
        if(pacString == null) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
        final String[] tokens = pacString.split(";");
        return Stream.of(tokens)
                .map(token -> token.trim())
                .map(token -> {
                    if (token.startsWith(PROXY)) {
                        return getInetSocketAddress(token)
                                .map(sa -> new Proxy(Proxy.Type.HTTP, sa))
                                .orElseThrow(() -> new IllegalArgumentException("HTTP Proxy must be specified with valid address"));
                    } else if (token.startsWith(SOCKS)) {
                        return getInetSocketAddress(token)
                                .map(sa -> new Proxy(Proxy.Type.SOCKS, sa))
                                .orElseThrow(() -> new IllegalArgumentException("SOCKS Proxy must be specified with valid address"));
                    } else if (token.startsWith(DIRECT)) {
                        return Proxy.NO_PROXY;
                    } else {
                        throw new IllegalArgumentException("Unrecognized proxy token: " + token);
                    }
                }).collect(Collectors.toList());

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
