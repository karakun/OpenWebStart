package com.openwebstart.proxy.linux;

import com.openwebstart.util.ProcessUtil;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.MANUAL;
import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.NO_PROXY;
import static com.openwebstart.proxy.linux.LinuxProxyProvider.LinuxProxyMode.PAC;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_AUTOCONFIG_URL;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_FTP_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_FTP_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTPS_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTPS_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTP_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTP_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_IGNORE_HOSTS;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_MODE;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_SOCKS_HOST;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_SOCKS_PORT;
import static com.openwebstart.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_USE_AUTHENTICATION;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

class GnomeProxyConfigReader {

    private static final Logger LOG = LoggerFactory.getLogger(GnomeProxyConfigReader.class);

    private static final Set<String> MAGIC_LOCALHOST_VALUES = new HashSet<>(asList("localhost", "127.0.0.1", "127.0.0.0/8"));
    private static final String MODE_NONE = "none";
    private static final String MODE_AUTO = "auto";
    private static final String MODE_MANUAL = "manual";

    public static Optional<LinuxProxySettings> readGnomeProxyConfig() {

        try {
            final String gnomeSettings = ProcessUtil.executeProcessAndReturnOutput("gsettings", "ist-recursively", "org.gnome.system.proxy");
            final Map<String, String> values = parseGnomeSettings(gnomeSettings);
            final LinuxProxySettings result = convertToSettings(values);
            return Optional.of(result);
        } catch (Exception e) {
            LOG.debug("Could not load gnome proxy settings {}", e.getMessage());
            return Optional.empty();
        }
    }


    /** visible for testing */
    static Map<String, String> parseGnomeSettings(String gnomeSettings) {
        final List<String> lines = splitIntoLines(gnomeSettings);

        return lines.stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.split("\\s+", 3))
                .filter(parts -> parts.length == 3)
                .collect(Collectors.toMap(parts -> parts[0] + ":" + parts[1], parts -> parts[2]));
    }

    /** visible for testing */
    static LinuxProxySettings convertToSettings(Map<String, String> values) throws MalformedURLException {
        final LinuxProxySettings result = new LinuxProxySettings();


        final String proxyMode = parseString(values.getOrDefault(GNOME_PROXY_MODE, MODE_NONE)).toLowerCase();
        if (MODE_NONE.equals(proxyMode)) {
            result.setMode(NO_PROXY);
        } else if (MODE_AUTO.equals(proxyMode)) {
            result.setMode(PAC);
            result.setAutoConfigUrl(new URL(parseString(values.get(GNOME_PROXY_AUTOCONFIG_URL))));
        } else if (MODE_MANUAL.equals(proxyMode)) {
            result.setMode(MANUAL);
            result.setAuthenticationEnabled(parseBoolean(values.get(GNOME_PROXY_USE_AUTHENTICATION)));
            result.setExceptionsList(parseList(values.get(GNOME_PROXY_IGNORE_HOSTS)));
            result.setLocalhostExcluded(containsLocalhost(result.getExceptionList()));

            // socks is fallback for all other types
            final String socksHost = parseString(values.get(GNOME_PROXY_SOCKS_HOST));
            final int socksPort = parseInt(values.get(GNOME_PROXY_SOCKS_PORT));
            if (!StringUtils.isBlank(socksHost) && socksPort > 0) {
                result.setSocksEnabled(true);
                result.setFtpEnabled(true);
                result.setHttpEnabled(true);
                result.setHttpsEnabled(true);

                result.setSocksHost(socksHost);
                result.setSocksPort(socksPort);

                result.setFtpHost(socksHost);
                result.setFtpPort(socksPort);

                result.setHttpHost(socksHost);
                result.setHttpPort(socksPort);

                result.setHttpsPort(socksPort);
                result.setHttpsHost(socksHost);
            }

            // ftp overwrites socks
            final String ftpHost = parseString(values.get(GNOME_PROXY_FTP_HOST));
            final int ftpPort = parseInt(values.get(GNOME_PROXY_FTP_PORT));
            if (!StringUtils.isBlank(ftpHost) && ftpPort > 0) {
                result.setFtpEnabled(true);
                result.setFtpPort(ftpPort);
                result.setFtpHost(ftpHost);
            }

            // http overwrites socks and is fallback for https
            final String httpHost = parseString(values.get(GNOME_PROXY_HTTP_HOST));
            final int httpPort = parseInt(values.get(GNOME_PROXY_HTTP_PORT));
            if (!StringUtils.isBlank(httpHost) && httpPort > 0) {
                result.setHttpEnabled(true);
                result.setHttpsEnabled(true);

                result.setHttpHost(httpHost);
                result.setHttpPort(httpPort);

                result.setHttpsHost(httpHost);
                result.setHttpsPort(httpPort);
            }

            // https overwrites both socks and http
            final String httpsHost = parseString(values.get(GNOME_PROXY_HTTPS_HOST));
            final int httpsPort = parseInt(values.get(GNOME_PROXY_HTTPS_PORT));
            if (!StringUtils.isBlank(httpsHost) && httpsPort > 0) {
                result.setHttpsEnabled(true);
                result.setHttpsPort(httpsPort);
                result.setHttpsHost(httpsHost);
            }

        } else {
            throw new RuntimeException("found unknown gnome proxy mode: " + proxyMode);
        }
        return result;
    }

    private static boolean containsLocalhost(List<String> exceptionList) {
        return exceptionList.stream().anyMatch(MAGIC_LOCALHOST_VALUES::contains);
    }

    private static List<String> splitIntoLines(String gnomeSettings) {
        final Scanner scanner = new Scanner(gnomeSettings);
        final List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        return lines;
    }

    private static String parseString(String s) {
        final String trimmed = s.trim();
        if (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length() > 1) {
            return trimmed.substring(1, trimmed.length() - 1);
        } else {
            throw new IllegalArgumentException("not a gnome string: " + s);
        }
    }

    private enum ParserState {
        START, OUTSIDE, INSIDE, END
    }

    private static List<String> parseList(String s) {
        final List<String> result = new ArrayList<>();
        StringBuilder next = new StringBuilder();
        ParserState state = ParserState.START;

        for (char c : s.trim().toCharArray()) {
            if (state == ParserState.START) {
                if (c == '[') {
                    state = ParserState.OUTSIDE;
                } else {
                    throw new IllegalArgumentException("Uninitialized list: " + s);
                }
            } else if (state == ParserState.OUTSIDE) {
                if (c == '\'') {
                    state = ParserState.INSIDE;
                } else if (c == ']') {
                    state = ParserState.END;
                }
            } else if (state == ParserState.INSIDE) {
                if (c == '\'') {
                    result.add(next.toString());
                    next = new StringBuilder();
                    state = ParserState.OUTSIDE;
                } else {
                    next.append(c);
                }
            } else {
                throw new IllegalArgumentException("Premature end of list: " + s);
            }
        }

        if (state != ParserState.END) {
            throw new IllegalArgumentException("Unterminated list: " + s);
        }

        return result;
    }
}
