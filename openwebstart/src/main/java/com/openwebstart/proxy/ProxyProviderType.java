package com.openwebstart.proxy;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.proxy.direct.DirectProxyProvider;
import com.openwebstart.proxy.firefox.FirefoxProxyProvider;
import com.openwebstart.proxy.linux.LinuxProxyProvider;
import com.openwebstart.proxy.mac.MacProxyProvider;
import com.openwebstart.proxy.manual.ManualConfigBasedProxyProvider;
import com.openwebstart.proxy.manual.ManualPacFileProxyProvider;
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
            return new ManualConfigBasedProxyProvider(config);
        }
    },

    MANUAL_PAC_URL(2) {
        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
            return new ManualPacFileProxyProvider(config);
        }
    },

    FIREFOX(3) {
        @Override
        public void checkSupported() {
            if (!isSupported()) {
                // Not implemented: https://support.mozilla.org/en-US/questions/1152265
                throw new IllegalStateException("Firefox proxy is not supported on " + OperationSystem.getLocalSystem());
            }
        }

        @Override
        public boolean isSupported() {
            //Not implemented: https://support.mozilla.org/en-US/questions/1152265
            return !OperationSystem.getLocalSystem().isMac();
        }

        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
            return new FirefoxProxyProvider(config);
        }
    },

    OPERATION_SYSTEM(4) {
        @Override
        public void checkSupported() {
            if (!isSupported()) {
                throw new IllegalStateException("System proxy is not supported for " + OperationSystem.getLocalSystem());
            }
        }

        @Override
        public boolean isSupported() {
            final OperationSystem localSystem = OperationSystem.getLocalSystem();
            return localSystem.isWindows() || localSystem.isMac() || localSystem.isLinux();
        }

        @Override
        public ProxyProvider createProvider(final DeploymentConfiguration config) throws Exception {
            final OperationSystem localSystem = OperationSystem.getLocalSystem();
            if (localSystem.isWindows()) {
                return new WindowsProxyProvider(config);
            } else if (localSystem.isMac()) {
                return new MacProxyProvider(config);
            } else if (localSystem.isLinux()) {
                return new LinuxProxyProvider(config);
            }
            throw new IllegalStateException("System proxy is not supported for " + OperationSystem.getLocalSystem());
        }
    };

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
        // subclasses implement specific behavior
    }

    public boolean isSupported() {
        // subclasses implement specific behavior
        return true;
    }

    public static ProxyProviderType getForConfigValue(final int value) {
        return Stream.of(ProxyProviderType.values())
                .filter(t -> value == t.getConfigValue())
                .findFirst()
                .filter(ProxyProviderType::isSupported)
                .orElse(NONE);
    }
}
