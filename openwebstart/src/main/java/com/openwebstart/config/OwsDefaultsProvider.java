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
    public static final String ALLOW_DOWNLOAD_SERVER_FROM_JNLP = "ows.jvm.manager.server.allowFromJnlp";
    public static final String JVM_VENDOR = "ows.jvm.manager.vendor";
    public static final String JVM_UPDATE_STRATEGY = "ows.jvm.manager.updateStrategy";
    public static final String JVM_SUPPORTED_VERSION_RANGE = "ows.jvm.manager.versionRange";

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
                        ALLOW_DOWNLOAD_SERVER_FROM_JNLP,
                        Boolean.FALSE.toString(),
                        ValidatorFactory.createBooleanValidator()
                ),
                Setting.createDefault(
                        JVM_VENDOR,
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
