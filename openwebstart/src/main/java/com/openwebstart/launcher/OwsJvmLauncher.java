package com.openwebstart.launcher;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.util.JavaExecutableFinder;
import com.openwebstart.jvm.util.JvmVersionUtils;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JREDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.Boot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

/**
 * Launches OWS with a JNLP in a matching JRE.
 */
class OwsJvmLauncher implements JvmLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(OwsJvmLauncher.class);

    private static final VersionString JAVA_1_8 = VersionString.fromString("1.8*");
    private static final VersionString JAVA_9_OR_GREATER = VersionString.fromString("9+");

    /**
     * The file "itw-modularjdk.args" can be found in the icedtea-web source.
     */
    private static final String ITW_MODULARJDK_ARGS = "itw-modularjdk.args";

    private final JavaRuntimeProvider javaRuntimeProvider;

    OwsJvmLauncher(JavaRuntimeProvider javaRuntimeProvider) {
        this.javaRuntimeProvider = javaRuntimeProvider;
    }

    @Override
    public void launchExternal(final JNLPFile jnlpFile, final List<String> args) throws Exception {
        final LocalJavaRuntime javaRuntime = getJavaRuntime(jnlpFile);
        LOG.info("using java runtime at '{}' for launching managed application", javaRuntime.getJavaHome());
        final File webStartJar = getOpenWebStartJar();
        launchExternal(javaRuntime, webStartJar, jnlpFile.getNewVMArgs(), args);
    }

    private LocalJavaRuntime getJavaRuntime(final JNLPFile jnlpFile) {
        for (JREDesc jre : jnlpFile.getResources().getJREs()) {
            final VersionString version = JvmVersionUtils.fromJnlp(jre.getVersion());
            LOG.debug("searching for JRE with version string '{}'", version);
            final LocalJavaRuntime javaRuntime = javaRuntimeProvider.getJavaRuntime(version, jre.getLocation());
            if (javaRuntime != null) {
                LOG.debug("Found JVM {}", javaRuntime);
                return javaRuntime;
            }
        }

        throw new IllegalStateException("could not find any suitable runtime");
    }

    /**
     * @param javaRuntime the JRE in which to start OWS
     * @param webstartJar the openwebstart.jar included in OWS
     * @param vmArgs      the arguments to pass to the jvm
     * @param javawsArgs  the arguments to pass to javaws (aka IcedTea-Web)
     */
    private void launchExternal(
            final LocalJavaRuntime javaRuntime,
            final File webstartJar,
            final List<String> vmArgs,
            final List<String> javawsArgs
    ) throws Exception {
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
        env.put(IcedTeaWebConstants.ICEDTEA_WEB_SPLASH, IcedTeaWebConstants.NO_SPLASH);

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
            final String remoteDebuggingPort = System.getProperty("OWS_REMOTE_DEBUGGING_PORT");
            if (!isBlank(remoteDebuggingPort)) {
                final int port = Integer.parseInt(remoteDebuggingPort);
                return Collections.singletonList("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + port);
            }
        } catch (Exception e) {
            LOG.error("Failed in adding remote debug args.", e);
        }
        return Collections.emptyList();
    }
}
