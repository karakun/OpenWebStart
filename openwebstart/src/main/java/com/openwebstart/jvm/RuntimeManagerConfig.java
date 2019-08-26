package com.openwebstart.jvm;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles.ItwCacheFileDescriptor;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.ALLOWS_NON_DEFAULT_JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;

public class RuntimeManagerConfig {

    public static final String KEY_USER_JVM_CACHE_DIR = "deployment.user.jvmcachedir";

    public static final ItwCacheFileDescriptor JVM_CACHE_DIR = new ItwCacheFileDescriptor("jvm-cache", "FILEjvmcache") {

        @Override
        public String getPropertiesKey() {
            return KEY_USER_JVM_CACHE_DIR;
        }
    };

    private static final RuntimeManagerConfig INSTANCE = new RuntimeManagerConfig();

    private RuntimeManagerConfig() {
    }

    public URI getDefaultRemoteEndpoint() {
        try {
            return new URI(config().getProperty(DEFAULT_JVM_DOWNLOAD_SERVER));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDefaultRemoteEndpoint(final URI defaultRemoteEndpoint) {
        config().setProperty(DEFAULT_JVM_DOWNLOAD_SERVER, defaultRemoteEndpoint.toString());
    }

    public boolean isSpecificRemoteEndpointsEnabled() {
        return Boolean.parseBoolean(config().getProperty(ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER));
    }

    public void setSpecificRemoteEndpointsEnabled(final boolean specificRemoteEndpointsEnabled) {
        config().setProperty(ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER, Boolean.toString(specificRemoteEndpointsEnabled));
    }

    public String getDefaultVendor() {
        return config().getProperty(DEFAULT_JVM_VENDOR);
    }

    public void setDefaultVendor(final String defaultVendor) {
        config().setProperty(DEFAULT_JVM_VENDOR, defaultVendor);
    }

    public boolean isSpecificVendorEnabled() {
        return Boolean.parseBoolean(config().getProperty(ALLOWS_NON_DEFAULT_JVM_VENDOR));
    }

    public void setSpecificVendorEnabled(final boolean specificVendorEnabled) {
        config().setProperty(ALLOWS_NON_DEFAULT_JVM_VENDOR, Boolean.toString(specificVendorEnabled));
    }

    public RuntimeUpdateStrategy getStrategy() {
        return RuntimeUpdateStrategy.valueOf(config().getProperty(JVM_UPDATE_STRATEGY));
    }

    public void setStrategy(final RuntimeUpdateStrategy strategy) {
        config().setProperty(JVM_UPDATE_STRATEGY, strategy.name());
    }

    public VersionString getSupportedVersionRange() {
        return VersionString.fromString(config().getProperty(JVM_SUPPORTED_VERSION_RANGE));
    }

    public void setSupportedVersionRange(final VersionString supportedVersionRange) {
        config().setProperty(JVM_SUPPORTED_VERSION_RANGE, supportedVersionRange.toString());
    }

    public Path getCachePath() {
        return Paths.get(JVM_CACHE_DIR.getFullPath());
    }

    public void setCachePath(final Path cachePath) {
        config().setProperty(KEY_USER_JVM_CACHE_DIR, cachePath.normalize().toAbsolutePath().toString());
    }

    public static RuntimeManagerConfig getInstance() {
        return INSTANCE;
    }

    private DeploymentConfiguration config() {
        return JNLPRuntime.getConfiguration();
    }
}
