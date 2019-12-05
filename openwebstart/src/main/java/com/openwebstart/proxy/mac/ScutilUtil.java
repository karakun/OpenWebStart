package com.openwebstart.proxy.mac;

import com.openwebstart.util.ProcessUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.proxy.mac.MacProxyProviderConstants.AUTO_CONFIG_ENABLED_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.AUTO_CONFIG_URL_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.AUTO_DISCOVERY_ENABLE_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.EXCEPTIONS_LIST_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.EXCLUDE_SIMPLE_HOSTNAMES_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.FTP_ENABLE_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.FTP_PASSIVE_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.FTP_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.FTP_PROXY_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.FTP_USER_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTPS_ENABLE_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTPS_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTPS_PROXY_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTPS_USER_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTP_ENABLE_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTP_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTP_PROXY_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.HTTP_USER_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.SOCKS_ENABLE_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.SOCKS_PORT_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.SOCKS_PROXY_PROPERTY_NAME;
import static com.openwebstart.proxy.mac.MacProxyProviderConstants.SOCKS_USER_PROPERTY_NAME;

public class ScutilUtil {

    public static MacProxySettings executeScutil() throws IOException, InterruptedException, ExecutionException {
        final Process process = new ProcessBuilder()
                .command("scutil", "--proxy")
                .redirectErrorStream(true)
                .start();
        final Future<String> out = ProcessUtil.getIO(process.getInputStream());
        final int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("process ended with error code " + exitValue);
        }
        final String processOut = out.get();
        return parse(processOut);
    }

    public static MacProxySettings parse(final String processOut) {
        final Scanner scanner = new Scanner(processOut);
        final List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }

        //Initial checks
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The given input can not be parsed!");
        }
        if (!Objects.equals("<dictionary> {", lines.get(0).trim())) {
            throw new IllegalArgumentException("The given input can not be parsed!");
        }
        if (!Objects.equals("}", lines.get(lines.size() - 1).trim())) {
            throw new IllegalArgumentException("The given input can not be parsed!");
        }

        //remove unneeded lines
        lines.remove(0);
        lines.remove(lines.size() - 1);

        final List<String> parameterLines = lines.stream()
                .map(l -> l.trim())
                .collect(Collectors.toList());

        final MacProxySettings proxySettings = new MacProxySettings();

        getValueForSimpleParam(HTTP_ENABLE_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setHttpEnabled);
        getValueForSimpleParam(HTTP_PROXY_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setHttpHost);
        getValueForSimpleParam(HTTP_PORT_PROPERTY_NAME, parameterLines)
                .map(Integer::parseInt)
                .ifPresent(proxySettings::setHttpPort);

        getValueForSimpleParam(HTTPS_ENABLE_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setHttpsEnabled);
        getValueForSimpleParam(HTTPS_PROXY_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setHttpsHost);
        getValueForSimpleParam(HTTPS_PORT_PROPERTY_NAME, parameterLines)
                .map(Integer::parseInt)
                .ifPresent(proxySettings::setHttpsPort);

        getValueForSimpleParam(FTP_ENABLE_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setFtpEnabled);
        getValueForSimpleParam(FTP_PROXY_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setFtpHost);
        getValueForSimpleParam(FTP_PORT_PROPERTY_NAME, parameterLines)
                .map(Integer::parseInt)
                .ifPresent(proxySettings::setFtpPort);

        getValueForSimpleParam(SOCKS_ENABLE_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setSocksEnabled);
        getValueForSimpleParam(SOCKS_PROXY_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setSocksHost);
        getValueForSimpleParam(SOCKS_PORT_PROPERTY_NAME, parameterLines)
                .map(Integer::parseInt)
                .ifPresent(proxySettings::setSocksPort);

        getValueForSimpleParam(AUTO_CONFIG_ENABLED_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setAutoConfigEnabled);
        getValueForSimpleParam(AUTO_CONFIG_URL_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::toUrl)
                .ifPresent(proxySettings::setAutoConfigUrl);

        getValueForSimpleParam(AUTO_DISCOVERY_ENABLE_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setAutoDiscoveryEnabled);

        getValueForSimpleParam(EXCLUDE_SIMPLE_HOSTNAMES_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setExcludeSimpleHostnames);

        getValueForSimpleParam(FTP_PASSIVE_PROPERTY_NAME, parameterLines)
                .map(ScutilUtil::asBooleanValue)
                .ifPresent(proxySettings::setFtpPassive);

        getValueForSimpleParam(HTTP_USER_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setHttpUser);
        getValueForSimpleParam(HTTPS_USER_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setHttpsUser);
        getValueForSimpleParam(FTP_USER_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setFtpUser);
        getValueForSimpleParam(SOCKS_USER_PROPERTY_NAME, parameterLines)
                .ifPresent(proxySettings::setSocksUser);

        proxySettings.setExceptionsList(getValueForArrayParam(EXCEPTIONS_LIST_PROPERTY_NAME, parameterLines));

        return proxySettings;
    }

    private static boolean asBooleanValue(final String value) {
        return Objects.equals("1", value);
    }

    private static URL toUrl(final String url) {
        try {
            return new URL(url);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Can not create URL for " + url, e);
        }
    }

    private static Optional<String> getValueForSimpleParam(final String paramName, final List<String> parameterLines) {
        return parameterLines.stream()
                .filter(l -> l.startsWith(paramName))
                .map(l -> l.substring(paramName.length() + 3))
                .findFirst();
    }

    private static List<String> getValueForArrayParam(final String paramName, final List<String> parameterLines) {
        return parameterLines
                .stream()
                .filter(l -> Objects.equals(paramName + " : <array> {", l.trim()))
                .findFirst()
                .map(l -> {
                    final int firstLine = parameterLines.indexOf(l);
                    final int lastLine = parameterLines.subList(firstLine, parameterLines.size()).indexOf("}");
                    return parameterLines.subList(firstLine + 1, firstLine + lastLine)
                            .stream()
                            .map(v -> v.split(Pattern.quote(":"))[1])
                            .map(v -> v.trim())
                            .collect(Collectors.toList());
                }).orElse(Collections.emptyList());
    }

}
