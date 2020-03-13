package com.openwebstart.jvm;

import com.openwebstart.config.OwsDefaultsProvider;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_DOWNLOAD_SERVER_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_VENDOR_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.MAX_DAYS_UNUSED_IN_JVM_CACHE;

public class RuntimeManagerConfig {

    private static DeploymentConfiguration deploymentConfiguration;

    private RuntimeManagerConfig() {
    }

    public static void setConfiguration(final DeploymentConfiguration deploymentConfiguration) {
        RuntimeManagerConfig.deploymentConfiguration = deploymentConfiguration;
    }

    public static URL getDefaultRemoteEndpoint() {
        try {

            final String defaultServer = config().getProperty(DEFAULT_JVM_DOWNLOAD_SERVER);
            return defaultServer != null ? new URL(defaultServer) : null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDefaultRemoteEndpoint(final URL defaultRemoteEndpoint) {
        final String defaultServer = defaultRemoteEndpoint != null ? defaultRemoteEndpoint.toString() : null;
        config().setProperty(DEFAULT_JVM_DOWNLOAD_SERVER, defaultServer);
    }

    public static boolean isNonDefaultServerAllowed() {
        return Boolean.parseBoolean(config().getProperty(ALLOW_DOWNLOAD_SERVER_FROM_JNLP));
    }

    public static void setNonDefaultServerAllowed(final boolean nonDefaultServerAllowed) {
        config().setProperty(ALLOW_DOWNLOAD_SERVER_FROM_JNLP, Boolean.toString(nonDefaultServerAllowed));
    }

    public static String getVendor() {
        return config().getProperty(JVM_VENDOR);
    }

    public static boolean isVendorFromJnlpAllowed() {
        return Boolean.parseBoolean(config().getProperty(ALLOW_VENDOR_FROM_JNLP));
    }

    public static void setVendorFromJnlpAllowed(final boolean vendorFromJnlpAllowed) {
        config().setProperty(ALLOW_VENDOR_FROM_JNLP, Boolean.toString(vendorFromJnlpAllowed));
    }

     public static int getMaxDaysUnusedInJvmCache() {
        return Integer.parseInt(config().getProperty(MAX_DAYS_UNUSED_IN_JVM_CACHE));
    }

    public static void setMaxDaysUnusedInJvmCache(final String maxDaysUnusedInJvmCache) {
        config().setProperty(MAX_DAYS_UNUSED_IN_JVM_CACHE, maxDaysUnusedInJvmCache);
    }

    public static void setDefaultVendor(final String defaultVendor) {
        config().setProperty(JVM_VENDOR, defaultVendor);
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
        config().setProperty(OwsDefaultsProvider.JVM_CACHE_DIR, cachePath.normalize().toAbsolutePath().toString());
    }

    private static DeploymentConfiguration config() {
        return deploymentConfiguration != null ? deploymentConfiguration : JNLPRuntime.getConfiguration();
    }
}
