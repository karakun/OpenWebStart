package com.openwebstart.install4j;

        import com.install4j.api.launcher.Variables;
        import com.install4j.runtime.installer.config.InstallerConfig;
        import com.install4j.runtime.installer.helper.InstallerUtil;
        import net.adoptopenjdk.icedteaweb.logging.Logger;
        import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

        import java.io.IOException;
        import java.util.Optional;

public class Install4JUtils {

    private static final Logger LOG = LoggerFactory.getLogger(Install4JUtils.class);

    private static final String UPDATES_URL_VARIABLE_NAME = "sys.updatesUrl";

    private static final String INSTALLATION_DIR_VARIABLE_NAME = "sys.installationDir";

    private static final String INSTALL4J_CONFIG_FILE = "i4jparams.conf";

    public static Optional<String> applicationVersion() {
        try {
            InstallerConfig config = InstallerConfig.getCurrentInstance();
            if (config == null) {
                config = InstallerConfig.getGeneralConfigFromFile(InstallerUtil.getInstallerFile(INSTALL4J_CONFIG_FILE));
            }
            return Optional.ofNullable(config.getApplicationVersion());
        } catch (IOException e) {
            LOG.warn("Can not read application applicationVersion");
            return Optional.empty();
        }
    }

    public static String updatesUrl() throws IllegalStateException {
        try {
            final String value = Variables.getCompilerVariable(UPDATES_URL_VARIABLE_NAME);
            if (value == null) {
                throw new IllegalStateException("No update url defined");
            }
            return value;
        } catch (Exception e) {
            throw new IllegalStateException("Can not get update url", e);
        }
    }

    public static Optional<String> installationDirectory() {
        return Install4JConfiguration.getInstance()
                .getInstallerVariableAsString​(INSTALLATION_DIR_VARIABLE_NAME);
    }

}
