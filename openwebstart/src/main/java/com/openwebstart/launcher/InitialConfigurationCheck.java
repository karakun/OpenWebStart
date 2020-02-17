package com.openwebstart.launcher;

import com.openwebstart.func.Result;
import com.openwebstart.install4j.Install4JConfiguration;
import com.openwebstart.update.UpdatePanelConfigConstants;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.Setting;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_DOWNLOAD_SERVER_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.MAX_DAYS_UNUSED_IN_JVM_CACHE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ASSUME_FILE_STEM_IN_CODEBASE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_COMPRESSION_ENABLED;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_MAX_SIZE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_TYPE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;
import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;

class InitialConfigurationCheck {

    private static final Logger LOG = LoggerFactory.getLogger(InitialConfigurationCheck.class);
    private static final String INSTALL4J_INSTALLATION_DATE_PROPERTY_NAME = "installationDate";
    private static final String LAST_UPDATE_PROPERTY_NAME = "ows.install4j.propertyUpdate";

    private final Install4JConfiguration install4JConfiguration;

    private final DeploymentConfiguration deploymentConfiguration;

    private final Lock preferencesStoreLock = new ReentrantLock();

    InitialConfigurationCheck(final DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = Assert.requireNonNull(deploymentConfiguration, "deploymentConfiguration");
        this.install4JConfiguration = Install4JConfiguration.getInstance();
    }

    void check() throws Exception {
        if (isFirstStart()) {
            LOG.debug("Looks like OpenWebStart is started for the first time. Will import initial configuration");

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
            initProperty(KEY_HTTPS_DONT_ENFORCE);
            initProperty(KEY_ASSUME_FILE_STEM_IN_CODEBASE);
            initProperty(KEY_SECURITY_SERVER_WHITELIST);
            initProperty(MAX_DAYS_UNUSED_IN_JVM_CACHE);
            initProperty(KEY_ENABLE_LOGGING);
            initProperty(KEY_ENABLE_LOGGING_TOFILE);

            initProperty(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME);
            initProperty(UpdatePanelConfigConstants.CHECK_FOR_UPDATED_NOW_PARAM_NAME);
            initProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME);
            initProperty(UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME);

            setLastUpdateProperty();

            deploymentConfiguration.save();
            getConfiguration().load(); // load the runtime config to make sure the initial config is applied.
            LOG.debug("Import of initial configuration done");
        }
    }

    private void initProperty(final String propertyName) {
        Assert.requireNonBlank(propertyName, "propertyName");

        LOG.debug("Checking if property '{}' is predefined", propertyName);

        final Map<String, Setting<String>> config = deploymentConfiguration.getRaw();

        install4JConfiguration.getInstallerVariableAsString(propertyName)
                .ifPresent(v -> {
                    LOG.debug("Property '{}' will be imported with value '{}'", propertyName, v);
                    config.computeIfAbsent(propertyName, this::createNewSetting).setValue(v);
                });

        install4JConfiguration.isVariableLocked(propertyName)
                .ifPresent(b -> {
                    LOG.debug("Lock of Property '{}' will be set to '{}'", propertyName, b);
                    config.computeIfAbsent(propertyName, this::createNewSetting).setLocked(b);
                });
    }

    private Setting<String> createNewSetting(String k) {
        return Setting.createUnknown(k, null);
    }

    private boolean isFirstStart() {
        preferencesStoreLock.lock();
        try {
            final long installationDate = Install4JConfiguration.getInstance()
                    .getInstallerVariableAsLong(INSTALL4J_INSTALLATION_DATE_PROPERTY_NAME)
                    .orElse(Long.MAX_VALUE);

            final Result<Long> lastPropertyUpdateDate = Optional.ofNullable(deploymentConfiguration.getProperty(LAST_UPDATE_PROPERTY_NAME))
                    .map(Result.of(Long::parseLong))
                    .orElse(Result.fail(new IllegalStateException("Time of last propertyUpdate not defined")));

            if (lastPropertyUpdateDate.isFailed()) {
                LOG.debug("Can not get '{}' property. Will do initial config", LAST_UPDATE_PROPERTY_NAME);
                return true;
            } else {
                LOG.debug("Checking if installation time ({}) is after last initial config time ({})", installationDate, lastPropertyUpdateDate.getResult());
                return installationDate > lastPropertyUpdateDate.getResult();
            }
        } finally {
            preferencesStoreLock.unlock();
        }
    }

    private void setLastUpdateProperty() {
        preferencesStoreLock.lock();
        try {
            deploymentConfiguration.setProperty(LAST_UPDATE_PROPERTY_NAME, Long.toString(System.currentTimeMillis()));
        } finally {
            preferencesStoreLock.unlock();
        }
    }
}
