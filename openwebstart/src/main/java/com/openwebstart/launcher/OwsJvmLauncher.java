package com.openwebstart.launcher;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.jvm.LocalRuntimeManager;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.util.JavaExecutableFinder;
import com.openwebstart.jvm.util.JvmVersionUtils;
import com.openwebstart.ui.Notifications;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JREDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.jvm.JvmUtils;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.debug.DebugParameterHelper.getRemoteDebugParameters;
import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.ICEDTEA_WEB_SPLASH;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.NO_SPLASH;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.ITW_BIN_LOCATION;
import static net.sourceforge.jnlp.util.logging.FileLog.LOG_POSTFIX_ENV;
import static net.sourceforge.jnlp.util.logging.FileLog.LOG_PREFIX_ENV;
import static net.sourceforge.jnlp.util.logging.FileLog.getLogFileNamePrefix;

/**
 * Launches OWS with a JNLP in a matching JRE.
 */
public class OwsJvmLauncher implements JvmLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(OwsJvmLauncher.class);

    private static final String INSTALL_4_J_EXE_DIR = "install4j.exeDir";
    private static final VersionString JAVA_1_8 = VersionString.fromString("1.8*");
    private static final VersionString JAVA_9_OR_GREATER = VersionString.fromString("9+");

    private final JavaRuntimeProvider javaRuntimeProvider;

    public OwsJvmLauncher(JavaRuntimeProvider javaRuntimeProvider) {
        this.javaRuntimeProvider = javaRuntimeProvider;
    }

    @Override
    public void launchExternal(final JNLPFile jnlpFile, final List<String> args) throws Exception {
        final File webStartJar = getOpenWebStartJar();
        launchExternal(jnlpFile, webStartJar, args);
    }

    private RuntimeInfo getLocalJavaRuntimeOrExit(final JNLPFile jnlpFile) {
        final Optional<RuntimeInfo> javaRuntime = getJavaRuntime(jnlpFile);
        if (!javaRuntime.isPresent()) {
            final Exception e = new IllegalStateException("could not find any suitable runtime");
            DialogFactory.showErrorDialog(Translator.getInstance().translate("jvmManager.error.noRuntimeFound"), e);
            return JNLPRuntime.exit(-1);
        }
        return javaRuntime.get();
    }

    Optional<RuntimeInfo> getJavaRuntime(final JNLPFile jnlpFile) {
        Assert.requireNonNull(jnlpFile, "jnlpFile");
        final List<JREDesc> jres = new ArrayList<>(Arrays.asList(jnlpFile.getResources().getJREs()));
        if (jres.isEmpty()) {
            jres.add(getDefaultJRE());
        }
        for (JREDesc jre : jres) {
            final VersionString version = JvmVersionUtils.fromJnlp(jre.getVersion());
            final Vendor vendor = Vendor.fromStringOrAny(jre.getVendor());
            final boolean require32bit = jre.isRequire32Bit();
            LOG.debug("searching for JRE with version string '{}' and vendor '{}'", version, vendor);
            try {
                final Optional<LocalJavaRuntime> javaRuntime = javaRuntimeProvider.getJavaRuntime(version, vendor, jre.getLocation(), require32bit);
                if (javaRuntime.isPresent()) {
                    LOG.debug("Found JVM {}", javaRuntime.get());
                    return javaRuntime.map(r -> new RuntimeInfo(r, jre));
                }
            } catch (final Exception e) {
                final String msg = Translator.getInstance().translate("jvmManager.error.downloadWithDetails", version, jre.getLocation());
                LOG.warn(msg, e);
                Notifications.showError(msg);
            }
        }

        return Optional.empty();
    }

    private JREDesc getDefaultJRE() {
        try {
            return new JREDesc(VersionString.fromString("1.8+"), null, false, null, null, null, null, null);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jnlpFile    the JNLPFile defining the application to launch
     * @param webstartJar the openwebstart.jar included in OWS
     * @param javawsArgs  the arguments to pass to javaws (aka IcedTea-Web)
     */
    private void launchExternal(
            final JNLPFile jnlpFile,
            final File webstartJar,
            final List<String> javawsArgs
    ) throws Exception {
        final RuntimeInfo runtimeInfo = getLocalJavaRuntimeOrExit(jnlpFile);
        LOG.info("using java runtime at '{}' for launching managed application", runtimeInfo.runtime.getJavaHome());

        final LocalJavaRuntime javaRuntime = runtimeInfo.runtime;
        final List<String> vmArgs = new ArrayList<>();
        getOwsExecutablePath().ifPresent(path -> vmArgs.add(propertyString(ITW_BIN_LOCATION, path)));
        vmArgs.addAll(runtimeInfo.jreDesc.getAllVmArgs());
        vmArgs.addAll(extractVmArgs(jnlpFile));

        final String pathToJavaBinary = JavaExecutableFinder.findJavaExecutable(javaRuntime.getJavaHome());
        final VersionId version = javaRuntime.getVersion();

        LocalRuntimeManager.touch(javaRuntime);

        if (JAVA_1_8.contains(version)) {
            launchExternal(pathToJavaBinary, webstartJar.getPath(), vmArgs, javawsArgs);
        } else if (JAVA_9_OR_GREATER.contains(version)) {
            List<String> mergedVMArgs = JvmUtils.mergeJavaModulesVMArgs(vmArgs);
            launchExternal(pathToJavaBinary, webstartJar.getPath(), mergedVMArgs, javawsArgs);
        } else {
            throw new RuntimeException("Java " + version + " is not supported");
        }


    }

    private List<String> extractVmArgs(final JNLPFile jnlpFile) {
        if (jnlpFile.getSecurity().getApplicationPermissionLevel() == ApplicationPermissionLevel.ALL) {
            final List<String> result = new ArrayList<>();
            final Map<String, String> properties = jnlpFile.getResources().getPropertiesMap();
            properties.keySet().forEach(key -> {
                if (JvmUtils.isValidSecureProperty(key)) {
                    result.add(propertyString(key, properties.get(key)));
                    LOG.debug("Set -D jvm arg for property {} from JNLP file properties map.", key);
                }
            });
            return result;
        }
        return Collections.emptyList();
    }

    private void launchExternal(
            final String pathToJavaBinary,
            final String pathToJar,
            final List<String> vmArgs,
            final List<String> javawsArgs
    ) throws IOException {
        final List<String> commands = new LinkedList<>();

        commands.add(quoteIfRequired(pathToJavaBinary));
        commands.add(quoteIfRequired("-Xbootclasspath/a:" + pathToJar));
        commands.addAll(vmArgs);
        commands.addAll(getRemoteDebuggingArgs());
        commands.add(Boot.class.getName());
        commands.addAll(javawsArgs);

        LOG.info("About to launch external with commands: '{}'", commands.toString());

        final ProcessBuilder pb = new ProcessBuilder();
        final Map<String, String> env = pb.environment();
        env.put(ICEDTEA_WEB_SPLASH, NO_SPLASH);
        env.put(LOG_PREFIX_ENV, getLogFileNamePrefix());
        env.put(LOG_POSTFIX_ENV, "ows-stage2");

        final Process p = pb
                .command(commands)
                .inheritIO()
                .start();

        ProcessUtils.waitForSafely(p);
    }

    private Optional<String> getOwsExecutablePath() {
        final String installDir = Install4JUtils.installationDirectory()
                .orElse(System.getProperty(INSTALL_4_J_EXE_DIR));

        if (installDir == null) {
            return Optional.empty();
        }

        final Path unixPath = Paths.get(installDir, JAVAWS);
        if (Files.isExecutable(unixPath)) {
            return Optional.ofNullable(unixPath.toAbsolutePath().toString());
        }

        final Path windowsPath = Paths.get(installDir, JAVAWS + ".exe");
        if (Files.isExecutable(windowsPath)) {
            return Optional.ofNullable(windowsPath.toAbsolutePath().toString());
        }

        return Optional.empty();
    }

    private static File getOpenWebStartJar() {
        final String classPath = System.getProperty("java.class.path");
        final String pathSeparator = System.getProperty("path.separator");
        final String javaHome = System.getProperty("java.home");
        final String[] classpathElements = classPath.split(Pattern.quote(pathSeparator));

        final List<File> jarCandidates = Arrays.stream(classpathElements)
                .filter((e) -> e.endsWith("openwebstart.jar"))
                .filter((e) -> !e.startsWith(javaHome))
                .map(File::new)
                .filter(File::exists)
                .map(File::getAbsoluteFile)
                .map(OwsJvmLauncher::canonicalFile)
                .collect(Collectors.toList());

        if (jarCandidates.size() == 1) {
            return jarCandidates.get(0);
        } else if (jarCandidates.size() > 1) {
            throw new IllegalStateException("multiple openwebstart jars found in classpath");
        } else {
            throw new IllegalStateException("openwebstart jar not found in classpath");
        }
    }

    private static File canonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getRemoteDebuggingArgs() {
        try {
            final DeploymentConfiguration configuration = JNLPRuntime.getConfiguration();
            final String debugActive = configuration.getProperty(OwsDefaultsProvider.REMOTE_DEBUG);
            if (Boolean.parseBoolean(debugActive)) {

                final String randomPortString = configuration.getProperty(OwsDefaultsProvider.RANDOM_DEBUG_PORT);
                final boolean usingAnyPort = Boolean.parseBoolean(randomPortString);

                final String startSuspendedString = configuration.getProperty(OwsDefaultsProvider.START_SUSPENDED);
                final boolean startSuspended = Boolean.parseBoolean(startSuspendedString);

                final String debugHost = configuration.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_HOST);

                final String debugPort = configuration.getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT);
                final int port = Integer.parseInt(debugPort);

                LOG.debug("Using Debug Host:" + usingAnyPort);
                LOG.debug("Using any port:" + usingAnyPort);
                LOG.debug("Start suspended:" + startSuspended);
                if (!usingAnyPort) {
                    // print only when relevant
                    LOG.debug("debug port " + port);
                }

                return Collections.singletonList(getRemoteDebugParameters(usingAnyPort, startSuspended, debugHost, port));
            }
        } catch (Exception e) {
            LOG.error("Failed in adding remote logging args.", e);
        }

        return Collections.emptyList();
    }

    private String propertyString(String key, String value) {
        return String.format("-D%s=%s", key, value);
    }

    private static class RuntimeInfo {
        private final LocalJavaRuntime runtime;
        private final JREDesc jreDesc;

        private RuntimeInfo(final LocalJavaRuntime runtime, final JREDesc jreDesc) {
            this.runtime = runtime;
            this.jreDesc = jreDesc;
        }
    }
}
