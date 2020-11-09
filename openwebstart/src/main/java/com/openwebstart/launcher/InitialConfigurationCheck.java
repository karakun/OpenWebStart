package com.openwebstart.launcher;

import com.openwebstart.func.Result;
import com.openwebstart.install4j.Install4JConfiguration;
import com.openwebstart.update.UpdatePanelConfigConstants;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_DOWNLOAD_SERVER_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.ALLOW_VENDOR_FROM_JNLP;
import static com.openwebstart.config.OwsDefaultsProvider.CUSTOM_JVM_LOCATION;
import static com.openwebstart.config.OwsDefaultsProvider.DEFAULT_JVM_DOWNLOAD_SERVER;
import static com.openwebstart.config.OwsDefaultsProvider.EXCLUDE_DEFAULT_JVM_LOCATION;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_CACHE_DIR;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SERVER_WHITELIST;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_SUPPORTED_VERSION_RANGE;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_UPDATE_STRATEGY;
import static com.openwebstart.config.OwsDefaultsProvider.JVM_VENDOR;
import static com.openwebstart.config.OwsDefaultsProvider.MAX_DAYS_UNUSED_IN_JVM_CACHE;
import static com.openwebstart.config.OwsDefaultsProvider.SEARCH_FOR_LOCAL_JVM_ON_STARTUP;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ASSUME_FILE_STEM_IN_CODEBASE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_MAX_SIZE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_DEBUG_LOGGING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_JVM_ARGS_WHITELIST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LIST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTPS_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTPS_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SAME;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_TYPE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;
import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;

class InitialConfigurationCheck {

    private static final Logger LOG = LoggerFactory.getLogger(InitialConfigurationCheck.class);
    private static final String INSTALL4J_INSTALLATION_DATE_PROPERTY_NAME = "installationDate";
    private static final String LAST_UPDATE_PROPERTY_NAME = "ows.install4j.propertyUpdate";

    private static final List<String> KEYS_OF_PROPERTIES_TO_TAKE_FROM_RESPONSE_VAR_FILE = Arrays.asList(
            JVM_CACHE_DIR,
            DEFAULT_JVM_DOWNLOAD_SERVER,
            ALLOW_DOWNLOAD_SERVER_FROM_JNLP,
            JVM_VENDOR,
            ALLOW_VENDOR_FROM_JNLP,
            JVM_SERVER_WHITELIST,
            JVM_UPDATE_STRATEGY,
            JVM_SUPPORTED_VERSION_RANGE,
            SEARCH_FOR_LOCAL_JVM_ON_STARTUP,
            EXCLUDE_DEFAULT_JVM_LOCATION,
            CUSTOM_JVM_LOCATION,
            KEY_PROXY_HTTP_HOST,
            KEY_PROXY_HTTPS_HOST,
            KEY_PROXY_HTTP_PORT,
            KEY_PROXY_HTTPS_PORT,
            KEY_PROXY_BYPASS_LOCAL,
            KEY_PROXY_BYPASS_LIST,
            KEY_PROXY_TYPE,
            KEY_PROXY_AUTO_CONFIG_URL,
            KEY_PROXY_SAME,
            KEY_CACHE_MAX_SIZE,
            KEY_HTTPS_DONT_ENFORCE,
            KEY_ASSUME_FILE_STEM_IN_CODEBASE,
            KEY_SECURITY_SERVER_WHITELIST,
            MAX_DAYS_UNUSED_IN_JVM_CACHE,
            KEY_ENABLE_DEBUG_LOGGING,
            KEY_ENABLE_LOGGING_TOFILE,
            KEY_JVM_ARGS_WHITELIST,

            UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME,
            UpdatePanelConfigConstants.CHECK_FOR_UPDATED_NOW_PARAM_NAME,
            UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME,
            UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME
    );

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

            KEYS_OF_PROPERTIES_TO_TAKE_FROM_RESPONSE_VAR_FILE.forEach(this::initProperty);

            setLastUpdateProperty();

            deploymentConfiguration.save();
            getConfiguration().load(); // load the runtime config to make sure the initial config is applied.
            LOG.debug("Import of initial configuration done");
        }
    }

    private void initProperty(final String propertyName) {
        Assert.requireNonBlank(propertyName, "propertyName");

        LOG.debug("Checking if property '{}' is predefined", propertyName);

        final Map<String, Setting> config = deploymentConfiguration.getRaw();

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

    private Setting createNewSetting(String k) {
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
