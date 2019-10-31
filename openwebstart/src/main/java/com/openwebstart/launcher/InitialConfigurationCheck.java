package com.openwebstart.launcher;

import com.openwebstart.install4j.Install4JConfiguration;
import com.openwebstart.update.UpdatePanelConfigConstants;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_DOWNLOAD_SERVER_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_VENDOR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_COMPRESSION_ENABLED;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_MAX_SIZE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_TYPE;

public class InitialConfigurationCheck {

    private final static Logger LOG = LoggerFactory.getLogger(InitialConfigurationCheck.class);

    private final static String FIRST_START_VARIABLE_NAME = "com.karakun.openwebstart.config.initial.firstStart";

    private final Install4JConfiguration install4JConfiguration;

    private final DeploymentConfiguration deploymentConfiguration;

    private final Lock preferencesStoreLock = new ReentrantLock();

    public InitialConfigurationCheck(final DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = Assert.requireNonNull(deploymentConfiguration, "deploymentConfiguration");
        this.install4JConfiguration = Install4JConfiguration.getInstance();
    }

    public void check() throws Exception {
        if (isFirstStart()) {
            LOG.info("Looks like OpenWebStart is started for the first time. Will import initial configuration");

            initProperty(DEFAULT_JVM_DOWNLOAD_SERVER);
            initProperty(ALLOW_DOWNLOAD_SERVER_FROM_JNLP);
            initProperty(JVM_VENDOR);
            initProperty(JVM_UPDATE_STRATEGY);
            initProperty(JVM_SUPPORTED_VERSION_RANGE);
            initProperty(KEY_PROXY_HTTP_HOST);
            initProperty(KEY_PROXY_HTTP_PORT);
            initProperty(KEY_PROXY_BYPASS_LOCAL);
            initProperty(KEY_PROXY_TYPE);
            initProperty(KEY_PROXY_AUTO_CONFIG_URL);
            initProperty(KEY_CACHE_MAX_SIZE);
            initProperty(KEY_CACHE_COMPRESSION_ENABLED);

            initProperty(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME);
            initProperty(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_NOW_PARAM_NAME);
            initProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME);
            initProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME);

            deploymentConfiguration.save();

            setFirstStartDoneFlag();
            LOG.info("Import of initial configuration done");
        }
    }

    private void initProperty(final String propertyName) {
        Assert.requireNonBlank(propertyName, "propertyName");

        LOG.info("Checking if property '{}' is predefined", propertyName);

        install4JConfiguration.getInstallerVariableAsString​(propertyName)
                .ifPresent(v -> {
                    LOG.info("Property '{}' will be imported with value '{}'", propertyName, v);
                    deploymentConfiguration.setProperty(propertyName, v);
                });

        if (install4JConfiguration.isVariableLocked(propertyName)) {
            LOG.info("Property '{}' will be locked", propertyName);
            deploymentConfiguration.lock(propertyName);
        } else {
            LOG.info("no lock defined for property '{}'", propertyName);
        }
    }

    public boolean isFirstStart() {
        preferencesStoreLock.lock();
        try {
            final boolean value = Optional.ofNullable(deploymentConfiguration.getProperty(FIRST_START_VARIABLE_NAME))
                    .map(s -> Boolean.parseBoolean(s))
                    .orElse(true);
            return value;
        } finally {
            preferencesStoreLock.unlock();
        }
    }

    public void setFirstStartDoneFlag() throws Exception {
        preferencesStoreLock.lock();
        try {
            deploymentConfiguration.setProperty(FIRST_START_VARIABLE_NAME, Boolean.FALSE.toString());
            deploymentConfiguration.save();
        } finally {
            preferencesStoreLock.unlock();
        }
    }
}
