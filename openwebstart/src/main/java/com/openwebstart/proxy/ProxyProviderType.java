package com.openwebstart.proxy;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.proxy.config.ConfigBasedAutoConfigUrlProxyProvider;
import com.openwebstart.proxy.config.ConfigBasedProxyProvider;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.firefox.FirefoxProxyProvider;
import com.openwebstart.proxy.windows.WindowsProxyProvider;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.util.stream.Stream;

public enum ProxyProviderType {

    NONE(0) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) {
            return DirectProxyProvider.getInstance();
        }
    },

    MANUAL_HOSTS(1) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) {
            return new ConfigBasedProxyProvider(config);
        }
    },

    MANUAL_PAC_URL(2) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
            return new ConfigBasedAutoConfigUrlProxyProvider(config);
        }
    },

    FIREFOX(3) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
            return new FirefoxProxyProvider();
        }
    },

    WINDOWS(4) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
            return new WindowsProxyProvider();
        }
    },

    ;

    private final int configValue;

    ProxyProviderType(final int configValue) {
        this.configValue = configValue;
    }

    public int getConfigValue() {
        return configValue;
    }

    public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
        throw new RuntimeException("this method must be overridden by every instance of the enum");
    }

    public void checkSupported() {
        if (this == WINDOWS && !OperationSystem.getLocalSystem().isWindows()) {
            throw new IllegalStateException("Windows proxy is only supported on windows os");
        }
        if (this == FIREFOX && OperationSystem.getLocalSystem().isMac()) {
            //Not implemented: https://support.mozilla.org/en-US/questions/1152265
            throw new IllegalStateException("Firefox proxy is not supported on mac os");
        }
    }

    public boolean isSupported() {
        if (this == WINDOWS && !OperationSystem.getLocalSystem().isWindows()) {
            return false;
        }
        if (this == FIREFOX && OperationSystem.getLocalSystem().isMac()) {
            //Not implemented: https://support.mozilla.org/en-US/questions/1152265
            return false;
        }
        return true;
    }

    public static ProxyProviderType getForConfigValue(final int value) {
        return Stream.of(ProxyProviderType.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .orElse(NONE);
    }
}
