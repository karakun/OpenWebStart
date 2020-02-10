package com.openwebstart.launcher;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.util.JavaExecutableFinder;
import com.openwebstart.jvm.util.JvmVersionUtils;
import com.openwebstart.ui.Notifications;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JREDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.jvm.runtimes.JavaRuntime.HTTP_AGENT_PROPERTY;
import static com.openwebstart.jvm.runtimes.JavaRuntime.HTTP_AUTH_DIGEST_VALIDATEPROXY_PROPERTY;
import static com.openwebstart.jvm.runtimes.JavaRuntime.HTTP_AUTH_DIGEST_VALIDATESERVER_PROPERTY;
import static com.openwebstart.jvm.runtimes.JavaRuntime.HTTP_MAX_REDIRECTS_PROPERTY;
import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.ICEDTEA_WEB_SPLASH;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.NO_SPLASH;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

/**
 * Launches OWS with a JNLP in a matching JRE.
 */
public class OwsJvmLauncher implements JvmLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(OwsJvmLauncher.class);

    private static final VersionString JAVA_1_8 = VersionString.fromString("1.8*");
    private static final VersionString JAVA_9_OR_GREATER = VersionString.fromString("9+");

    /**
     * The file "itw-modularjdk.args" can be found in the icedtea-web source.
     */
    private static final String ITW_MODULARJDK_ARGS = "itw-modularjdk.args";
    public static final String REMOTE_DEBUGGING_PREFIX = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=";
    public static final String REMOTE_DEBUGGING_SYSTEM_PROPERTY = "OWS_REMOTE_DEBUGGING_PORT";

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

    private Optional<RuntimeInfo> getJavaRuntime(final JNLPFile jnlpFile) {
        final List<JREDesc> jres = new ArrayList<>(Arrays.asList(jnlpFile.getResources().getJREs()));
        if (jres.isEmpty()) {
            jres.add(getDefaultJRE());
        }
        for (JREDesc jre : jres) {
            final VersionString version = JvmVersionUtils.fromJnlp(jre.getVersion());
            LOG.debug("searching for JRE with version string '{}'", version);
            try {
                final Optional<LocalJavaRuntime> javaRuntime = javaRuntimeProvider.getJavaRuntime(version, jre.getLocation());
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
            return new JREDesc(VersionString.fromString("1.8+"), null, null, null, null, null);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jnlpFile the JNLPFile defining the application to launch
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
        final List<String> vmArgs = new ArrayList<>(runtimeInfo.jreDesc.getAllVmArgs());

        vmArgs.addAll(extractVmArgs(jnlpFile));

        final String pathToJavaBinary = JavaExecutableFinder.findJavaExecutable(javaRuntime.getJavaHome());
        final VersionId version = javaRuntime.getVersion();

        if (JAVA_1_8.contains(version)) {
            launchExternal(pathToJavaBinary, webstartJar.getPath(), vmArgs, javawsArgs);
        } else if (JAVA_9_OR_GREATER.contains(version)) {
            vmArgs.add(quoteIfRequired('@' + webstartJar.getParent() + File.separator + ITW_MODULARJDK_ARGS));
            launchExternal(pathToJavaBinary, webstartJar.getPath(), vmArgs, javawsArgs);
        } else {
            throw new RuntimeException("Java " + version + " is not supported");
        }
    }

    private List<String> extractVmArgs(final JNLPFile jnlpFile) {
        if (jnlpFile.getSecurity().getApplicationPermissionLevel() == ApplicationPermissionLevel.ALL) {
            final List<String> result = new ArrayList<>();

            final Map<String, String> properties = jnlpFile.getResources().getPropertiesMap();

            if (properties.containsKey(HTTP_AGENT_PROPERTY)) {
                addVmArg(result, properties, HTTP_AGENT_PROPERTY);
            }
            if (properties.containsKey(HTTP_MAX_REDIRECTS_PROPERTY)) {
                addVmArg(result, properties, HTTP_MAX_REDIRECTS_PROPERTY);
            }
            if (properties.containsKey(HTTP_AUTH_DIGEST_VALIDATEPROXY_PROPERTY)) {
                addVmArg(result, properties, HTTP_AUTH_DIGEST_VALIDATEPROXY_PROPERTY);
            }
            if (properties.containsKey(HTTP_AUTH_DIGEST_VALIDATESERVER_PROPERTY)) {
                addVmArg(result, properties, HTTP_AUTH_DIGEST_VALIDATESERVER_PROPERTY);
            }
            return result;
        }
        return Collections.emptyList();
    }

    private void addVmArg(final List<String> result, final Map<String, String> properties, final String argumentName) {
        result.add(String.format("-D%s=%s", argumentName, properties.get(argumentName)));
        LOG.info("Set HTTP {} from JNLP file properties map.", argumentName);
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
        pb.environment().put(ICEDTEA_WEB_SPLASH, NO_SPLASH);

        final Process p = pb
                .command(commands)
                .inheritIO()
                .start();

        ProcessUtils.waitForSafely(p);
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
            final String remoteDebuggingPort = System.getProperty(REMOTE_DEBUGGING_SYSTEM_PROPERTY);
            if (!isBlank(remoteDebuggingPort)) {
                final int port = Integer.parseInt(remoteDebuggingPort);
                LOG.debug("Adding remote debug support on port " + port);
                return Collections.singletonList(REMOTE_DEBUGGING_PREFIX + port);
            }
        } catch (Exception e) {
            LOG.error("Failed in adding remote debugging args.", e);
        }

        try {
            final String debugActive = JNLPRuntime.getConfiguration().getProperty(OwsDefaultsProvider.REMOTE_DEBUG);
            if (Boolean.valueOf(debugActive)) {
                final String debugPort = JNLPRuntime.getConfiguration().getProperty(OwsDefaultsProvider.REMOTE_DEBUG_PORT);
                final int port = Integer.parseInt(debugPort);
                LOG.debug("Adding remote debug support on port " + port);
                return Collections.singletonList(REMOTE_DEBUGGING_PREFIX + port);
            }
        } catch (Exception e) {
            LOG.error("Failed in adding remote logging args.", e);
        }


        return Collections.emptyList();
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
