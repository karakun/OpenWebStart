package com.openwebstart.jvm;

import com.openwebstart.jvm.runtimes.RuntimeUpdateStrategy;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RuntimeManagerConfig {

    private static final RuntimeManagerConfig INSTANCE = new RuntimeManagerConfig();

    private final AtomicBoolean specificRemoteEndpointsEnabled = new AtomicBoolean();

    private final AtomicBoolean specificVendorEnabled = new AtomicBoolean();

    private final AtomicReference<String> defaultVendor = new AtomicReference<>();

    private final AtomicReference<URI> defaultRemoteEndpoint = new AtomicReference<>();

    private final AtomicReference<RuntimeUpdateStrategy> strategy = new AtomicReference<>(RuntimeUpdateStrategy.ASK_FOR_UPDATE_ON_LOCAL_MATCH);

    private final AtomicReference<Path> cachePath = new AtomicReference<>();

    private final AtomicReference<VersionString> supportedVersionRange = new AtomicReference<>();

    private RuntimeManagerConfig() {
    }

    public URI getDefaultRemoteEndpoint() {
        return defaultRemoteEndpoint.get();
    }

    public void setDefaultRemoteEndpoint(final URI defaultRemoteEndpoint) {
        this.defaultRemoteEndpoint.set(defaultRemoteEndpoint);
    }

    public boolean isSpecificRemoteEndpointsEnabled() {
        return specificRemoteEndpointsEnabled.get();
    }

    public void setSpecificRemoteEndpointsEnabled(final boolean specificRemoteEndpointsEnabled) {
        this.specificRemoteEndpointsEnabled.set(specificRemoteEndpointsEnabled);
    }

    public String getDefaultVendor() {
        return defaultVendor.get();
    }

    public void setDefaultVendor(final String defaultVendor) {
        this.defaultVendor.set(defaultVendor);
    }

    public boolean isSpecificVendorEnabled() {
        return specificVendorEnabled.get();
    }

    public void setSpecificVendorEnabled(final boolean specificVendorEnabled) {
        this.specificVendorEnabled.set(specificVendorEnabled);
    }

    public RuntimeUpdateStrategy getStrategy() {
        return strategy.get();
    }

    public void setStrategy(final RuntimeUpdateStrategy strategy) {
        this.strategy.set(strategy);
    }

    public VersionString getSupportedVersionRange() {
        return supportedVersionRange.get();
    }

    public void setSupportedVersionRange(final VersionString supportedVersionRange) {
        this.supportedVersionRange.set(supportedVersionRange);
    }

    public Path getCachePath() {
        return cachePath.get();
    }

    public void setCachePath(final Path cachePath) {
        this.cachePath.set(cachePath);
    }

    public static RuntimeManagerConfig getInstance() {
        return INSTANCE;
    }
}
