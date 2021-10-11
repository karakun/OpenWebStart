package com.openwebstart.jvm;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.jvm.runtimes.Vendor;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;
import net.sourceforge.jnlp.util.whitelist.WhitelistEntry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_DOWNLOAD_SERVER_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_VENDOR_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SERVER_WHITELIST;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.MAX_DAYS_UNUSED_IN_JVM_CACHE;

public class RuntimeManagerConfig {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeManagerConfig.class);

    private static DeploymentConfiguration deploymentConfiguration;
    private static List<WhitelistEntry> jvmServerWhitelist;

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
        try {
            migrateVendorIfRequired();
        } catch (IOException exception) {
            LOG.error("Failed to migration of outdated vendor.");
        }
        return config().getProperty(JVM_VENDOR);
    }

    private static void migrateVendorIfRequired() throws IOException {
        String vendorFromDeplyomentConfiguration = config().getProperty(JVM_VENDOR);
        Vendor currentVendor = Vendor.fromStringOrAny(vendorFromDeplyomentConfiguration);

        if (!Objects.equals(currentVendor.toString(), vendorFromDeplyomentConfiguration)) {
            if (config().isLocked(JVM_VENDOR)) {
                LOG.warn("Found outdated JVM vendor in system config '{}'. Please contact the administrators to migrate this to '{}'", vendorFromDeplyomentConfiguration, currentVendor);
            } else {
                LOG.info("Outdated JVM vendor setting detected. Silently migrate the JVM vendor from '{}' to '{}'", vendorFromDeplyomentConfiguration, currentVendor);
                LOG.info("If you are using unattended installation to the push config to the clients please consider updating your central configuration.");
                deploymentConfiguration.setProperty(JVM_VENDOR, currentVendor.toString());
                deploymentConfiguration.save();
            }
        }
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

    public static List<WhitelistEntry> getJvmServerWhitelist() {
        if (jvmServerWhitelist == null) {
            jvmServerWhitelist = UrlWhiteListUtils.loadWhitelistFromConfiguration(JVM_SERVER_WHITELIST);
        }
        return jvmServerWhitelist;
    }
}
