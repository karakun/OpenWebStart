package com.openwebstart.config;

import com.install4j.api.update.UpdateSchedule;
import com.openwebstart.jvm.PathAndFiles;
import com.openwebstart.jvm.RuntimeManagerConfig;
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


    public static final String REMOTE_DEBUG = "ows.jvm.manager.remoteDebug";
    public static final String REMOTE_DEBUG_PORT = "ows.jvm.manager.remoteDebugPort";

    public static final String DEFAULT_JVM_DOWNLOAD_SERVER = "ows.jvm.manager.server.default";
    public static final String ALLOW_DOWNLOAD_SERVER_FROM_JNLP = "ows.jvm.manager.server.allowFromJnlp";
    public static final String JVM_VENDOR = "ows.jvm.manager.vendor";
    public static final String JVM_UPDATE_STRATEGY = "ows.jvm.manager.updateStrategy";
    public static final String JVM_SUPPORTED_VERSION_RANGE = "ows.jvm.manager.versionRange";

    public static final String PROXY_PAC_CACHE = "deployment.proxy.pac.cache";

    public static final RuntimeUpdateStrategy DEFAULT_UPDATE_STRATEGY = RuntimeUpdateStrategy.ASK_FOR_UPDATE_ON_LOCAL_MATCH;

    @Override
    public List<Setting<String>> getDefaults() {
        return Arrays.asList(
                Setting.createDefault(
                        PROXY_PAC_CACHE,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        REMOTE_DEBUG,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        REMOTE_DEBUG_PORT,
                        "5005",
                        ValidatorFactory.createRangedIntegerValidator(0, 10_000)
                ),
                Setting.createDefault(
                        RuntimeManagerConfig.KEY_USER_JVM_CACHE_DIR,
                        PathAndFiles.JVM_CACHE_DIR.getDefaultFullPath(),
                        ValidatorFactory.createFilePathValidator()
                ),
                Setting.createDefault(
                        DEFAULT_JVM_DOWNLOAD_SERVER,
                        "https://download-openwebstart.com/jvms",
                        ValidatorFactory.createUrlValidator()
                ),
                Setting.createDefault(
                        ALLOW_DOWNLOAD_SERVER_FROM_JNLP,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        JVM_VENDOR,
                        Vendor.ANY_VENDOR.getName(),
                        null
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
                        "1.8+",
                        null
                ),
                Setting.createDefault(
                        UpdatePanelConfigConstants.CHECK_FOR_UPDATED_PARAM_NAME,
                        "true",
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
                )
        );
    }
}
