package com.openwebstart.jvm;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.ALLOWS_NON_DEFAULT_JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;

public class RuntimeManagerConfig {

    public static final String KEY_USER_JVM_CACHE_DIR = "deployment.user.jvmcachedir";

    private RuntimeManagerConfig() {
    }

    public static URI getDefaultRemoteEndpoint() {
        try {
            return new URI(config().getProperty(DEFAULT_JVM_DOWNLOAD_SERVER));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDefaultRemoteEndpoint(final URI defaultRemoteEndpoint) {
        config().setProperty(DEFAULT_JVM_DOWNLOAD_SERVER, defaultRemoteEndpoint.toString());
    }

    public static boolean isSpecificRemoteEndpointsEnabled() {
        return Boolean.parseBoolean(config().getProperty(ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER));
    }

    public static void setSpecificRemoteEndpointsEnabled(final boolean specificRemoteEndpointsEnabled) {
        config().setProperty(ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER, Boolean.toString(specificRemoteEndpointsEnabled));
    }

    public static String getDefaultVendor() {
        return config().getProperty(DEFAULT_JVM_VENDOR);
    }

    public static void setDefaultVendor(final String defaultVendor) {
        config().setProperty(DEFAULT_JVM_VENDOR, defaultVendor);
    }

    public static boolean isSpecificVendorEnabled() {
        return Boolean.parseBoolean(config().getProperty(ALLOWS_NON_DEFAULT_JVM_VENDOR));
    }

    public static void setSpecificVendorEnabled(final boolean specificVendorEnabled) {
        config().setProperty(ALLOWS_NON_DEFAULT_JVM_VENDOR, Boolean.toString(specificVendorEnabled));
    }

    public static RuntimeUpdateStrategy getStrategy() {
        final String name = config().getProperty(JVM_UPDATE_STRATEGY);
        return name != null ? RuntimeUpdateStrategy.valueOf(name) : DEFAULT_UPDATE_STRATEGY;
    }

    public static void setStrategy(final RuntimeUpdateStrategy strategy) {
        final String name = strategy != null ? strategy.name() : DEFAULT_UPDATE_STRATEGY.name();
        config().setProperty(JVM_UPDATE_STRATEGY, name);
    }

    public static VersionString getSupportedVersionRange() {
        final String version = config().getProperty(JVM_SUPPORTED_VERSION_RANGE);
        return version != null ? VersionString.fromString(version) : null;
    }

    public static void setSupportedVersionRange(final VersionString supportedVersionRange) {
        final String version = supportedVersionRange != null ? supportedVersionRange.toString() : null;
        config().setProperty(JVM_SUPPORTED_VERSION_RANGE, version);
    }

    public static Path getCachePath() {
        return Paths.get(PathAndFiles.JVM_CACHE_DIR.getFullPath());
    }

    public static void setCachePath(final Path cachePath) {
        config().setProperty(KEY_USER_JVM_CACHE_DIR, cachePath.normalize().toAbsolutePath().toString());
    }

    private static DeploymentConfiguration config() {
        return JNLPRuntime.getConfiguration();
    }
}
