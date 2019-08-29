package com.openwebstart.config;

import com.openwebstart.jvm.PathAndFiles;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.jvm.RuntimeUpdateStrategy;
import net.adoptopenjdk.icedteaweb.config.ValidatorFactory;
import net.sourceforge.jnlp.config.DefaultsProvider;
import net.sourceforge.jnlp.config.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class OwsDefaultsProvider implements DefaultsProvider {

    public static final String DEFAULT_JVM_DOWNLOAD_SERVER = "ows.jvm.manager.server.default";
    public static final String ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER = "ows.jvm.manager.server.allowOthers";
    public static final String DEFAULT_JVM_VENDOR = "ows.jvm.manager.vendor.default";
    public static final String JVM_UPDATE_STRATEGY = "ows.jvm.manager.update.strategy";
    public static final String JVM_SUPPORTED_VERSION_RANGE = "ows.jvm.manager.version.range";

    public static final RuntimeUpdateStrategy DEFAULT_UPDATE_STRATEGY = RuntimeUpdateStrategy.ASK_FOR_UPDATE_ON_LOCAL_MATCH;

    @Override
    public List<Setting<String>> getDefaults() {
        return Arrays.asList(
                Setting.createDefault(
                        RuntimeManagerConfig.KEY_USER_JVM_CACHE_DIR,
                        PathAndFiles.JVM_CACHE_DIR.getDefaultFullPath(),
                        ValidatorFactory.createFilePathValidator()
                ),
                Setting.createDefault(
                        DEFAULT_JVM_DOWNLOAD_SERVER,
                        "https://jvms.openwebstart.com/jvms",
                        ValidatorFactory.createUrlValidator()
                ),
                Setting.createDefault(
                        ALLOWS_NON_DEFAULT_JVM_DOWNLOAD_SERVER,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        DEFAULT_JVM_VENDOR,
                        "AdoptOpenJDK",
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
                )
        );
    }
}
