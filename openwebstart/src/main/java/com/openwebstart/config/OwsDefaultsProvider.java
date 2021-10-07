package com.openwebstart.config;

import com.install4j.api.update.UpdateSchedule;
import com.openwebstart.jvm.PathAndFiles;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.update.UpdatePanelConfigConstants;
import net.adoptopenjdk.icedteaweb.config.ValidatorFactory;
import net.sourceforge.jnlp.config.DefaultsProvider;
import net.sourceforge.jnlp.config.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class OwsDefaultsProvider implements DefaultsProvider {

    public static final String REMOTE_DEBUG = "ows.remote.debug.enabled";
    public static final String START_SUSPENDED = "ows.remote.debug.startSuspended";
    public static final String RANDOM_DEBUG_PORT = "ows.remote.debug.randomDebugPort";
    public static final String REMOTE_DEBUG_PORT = "ows.remote.debug.fixedPort";
    public static final String REMOTE_DEBUG_PORT_DEFAULT_VALUE = "5005";
    public static final String REMOTE_DEBUG_HOST = "ows.remote.debug.host";
    public static final String REMOTE_DEBUG_HOST_DEFAULT_VALUE = "127.0.0.1";

    public static final String DEFAULT_JVM_DOWNLOAD_SERVER = "ows.jvm.manager.server.default";
    public static final String DEFAULT_JVM_DOWNLOAD_SERVER_DEFAULT_VALUE = "https://download-openwebstart.com/jvms.json";
    public static final String ALLOW_DOWNLOAD_SERVER_FROM_JNLP = "ows.jvm.manager.server.allowFromJnlp";
    public static final String JVM_SERVER_WHITELIST = "ows.jvm.manager.server.allowFromJnlp.whitelist";
    public static final String JVM_CACHE_DIR = "ows.jvm.manager.cache.dir";
    public static final String JVM_CACHE_CLEANUP_ENABLED = "ows.jvm.manager.cache.cleanup";
    public static final String JVM_VENDOR = "ows.jvm.manager.vendor";
    public static final String ALLOW_VENDOR_FROM_JNLP = "ows.jvm.manager.vendor.allowFromJnlp";
    public static final String JVM_UPDATE_STRATEGY = "ows.jvm.manager.updateStrategy";
    public static final String JVM_SUPPORTED_VERSION_RANGE = "ows.jvm.manager.versionRange";
    public static final String JVM_SUPPORTED_VERSION_RANGE_DEFAULT_VALUE = "1.8+";

    public static final String MAX_DAYS_UNUSED_IN_JVM_CACHE = "ows.jvm.manager.maxDaysUnusedInJvmCache";
    public static final String MAX_DAYS_UNUSED_IN_JVM_CACHE_DEFAULT_VALUE = "30";

    public static final String SEARCH_FOR_LOCAL_JVM_ON_STARTUP = "ows.jvm.manager.searchLocalAtStartup";
    public static final String EXCLUDE_DEFAULT_JVM_LOCATION = "ows.jvm.manager.excludeDefaultSearchLocation";
    public static final String CUSTOM_JVM_LOCATION = "ows.jvm.manager.customSearchLocation";


    public static final String PROXY_PAC_CACHE = "deployment.proxy.pac.cache";

    public static final String SHOW_PROXY_UNSUPPORTED_NOTIFICATIONS = "ows.jvm.proxy.unsupportedFeature.showNotification";

    public static final String APPLICATION_MANAGER_ACTIVE = "ows.experimental.applicationManager.active";


    public static final RuntimeUpdateStrategy DEFAULT_UPDATE_STRATEGY = RuntimeUpdateStrategy.ASK_FOR_UPDATE_ON_LOCAL_MATCH;

    @Override
    public List<Setting> getDefaults() {
        return Arrays.asList(
                Setting.createDefault(
                        "ows.install4j.propertyUpdate",
                        null,
                        null
                ),
                Setting.createDefault(
                        SHOW_PROXY_UNSUPPORTED_NOTIFICATIONS,
                        Boolean.TRUE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        PROXY_PAC_CACHE,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        APPLICATION_MANAGER_ACTIVE,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        REMOTE_DEBUG,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        START_SUSPENDED,
                        Boolean.TRUE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        RANDOM_DEBUG_PORT,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        REMOTE_DEBUG_PORT,
                        REMOTE_DEBUG_PORT_DEFAULT_VALUE,
                        ValidatorFactory.createRangedIntegerValidator(0, 10_000)
                ),
                Setting.createDefault(
                        REMOTE_DEBUG_HOST,
                        REMOTE_DEBUG_HOST_DEFAULT_VALUE,
                        ValidatorFactory.createNotBlankValidator()
                ),
                Setting.createDefault(
                        JVM_CACHE_DIR,
                        PathAndFiles.JVM_CACHE_DIR.getDefaultFullPath(),
                        ValidatorFactory.createFilePathValidator()
                ),
                Setting.createDefault(
                        JVM_CACHE_CLEANUP_ENABLED,
                        Boolean.TRUE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        DEFAULT_JVM_DOWNLOAD_SERVER,
                        DEFAULT_JVM_DOWNLOAD_SERVER_DEFAULT_VALUE,
                        ValidatorFactory.createUrlValidator()
                ),
                Setting.createDefault(
                        ALLOW_DOWNLOAD_SERVER_FROM_JNLP,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        JVM_SERVER_WHITELIST,
                        null,
                        null
                ),
                Setting.createDefault(
                        JVM_VENDOR,
                        Vendor.ANY_VENDOR.getName(),
                        null
                ),
                Setting.createDefault(
                        ALLOW_VENDOR_FROM_JNLP,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        JVM_UPDATE_STRATEGY,
                        DEFAULT_UPDATE_STRATEGY.name(),
                        ValidatorFactory.createStringValidator(
                                Stream.of(RuntimeUpdateStrategy.values())
                                        .map(Enum::name)
                                        .toArray(String[]::new)
                        )
                ),
                Setting.createDefault(
                        JVM_SUPPORTED_VERSION_RANGE,
                        JVM_SUPPORTED_VERSION_RANGE_DEFAULT_VALUE,
                        null
                ),
                Setting.createDefault(
                        UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME,
                        Boolean.TRUE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        UpdatePanelConfigConstants.UPDATED_STRATEGY_SETTINGS_PARAM_NAME,
                        UpdateSchedule.ON_EVERY_START.name(),
                        ValidatorFactory.createStringValidator(
                                Stream.of(UpdateSchedule.values())
                                        .map(Enum::name)
                                        .toArray(String[]::new)
                        )
                ),
                Setting.createDefault(
                        UpdatePanelConfigConstants.UPDATED_STRATEGY_LAUNCH_PARAM_NAME,
                        UpdateSchedule.WEEKLY.name(),
                        ValidatorFactory.createStringValidator(
                                Stream.of(UpdateSchedule.values())
                                        .map(Enum::name)
                                        .toArray(String[]::new)
                        )
                ),
                Setting.createDefault(
                        MAX_DAYS_UNUSED_IN_JVM_CACHE,
                        MAX_DAYS_UNUSED_IN_JVM_CACHE_DEFAULT_VALUE,
                        ValidatorFactory.createRangedIntegerValidator(0, 3_650)
                ),
                Setting.createDefault(
                        SEARCH_FOR_LOCAL_JVM_ON_STARTUP,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        EXCLUDE_DEFAULT_JVM_LOCATION,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        CUSTOM_JVM_LOCATION,
                        null,
                        null
                )
        );
    }
}
