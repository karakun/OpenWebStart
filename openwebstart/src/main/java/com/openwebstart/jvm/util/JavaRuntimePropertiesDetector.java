package com.openwebstart.jvm.util;

import com.openwebstart.func.Result;
import com.openwebstart.launcher.OwsJvmLauncher;
import com.openwebstart.util.ProcessResult;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.openwebstart.jvm.os.OperationSystem.OS_BITNESS;
import static com.openwebstart.util.ProcessUtil.runProcess;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VENDOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_NAME;

/**
 * Algorithm to extract the Java Runtime Properties by executing "java -XshowSettings:properties -version"
 */
public final class JavaRuntimePropertiesDetector {

    private static final Logger LOG = LoggerFactory.getLogger(JavaRuntimePropertiesDetector.class);

    private static final Set<String> REQUIRED_PROPS = unmodifiableSet(new HashSet<>(asList(
            JAVA_VENDOR,
            JAVA_VERSION,
            OS_NAME,
            OS_ARCH,
            OS_BITNESS
    )));

    private static final String SHOW_SETTINGS_ARG = "-XshowSettings:properties";
    private static final String VERSION_ARG = "-version";
    private static final String CP_ARG = "-cp";

    private JavaRuntimePropertiesDetector() {
        // Utility class, do not instantiate.
    }

    public static JavaRuntimeProperties getProperties(Path javaHome) throws IllegalStateException {
        LOG.info("Trying to get definition of local JVM at '{}'", javaHome);
        final String java = JavaExecutableFinder.findJavaExecutable(javaHome);

        final Result<JavaRuntimeProperties> fromShowSettings = fetchRuntimeProperties(java, JavaRuntimePropertiesDetector::showSettings);
        if (fromShowSettings.isSuccessful()) {
            return fromShowSettings.getResult();
        }

        final Result<JavaRuntimeProperties> fromPropertiesPrinter = fetchRuntimeProperties(java, JavaRuntimePropertiesDetector::showSystemProperties);
        if (fromPropertiesPrinter.isSuccessful()) {
            return fromPropertiesPrinter.getResult();
        }

        final String message = String.format("Can not get properties for JVM in path '%s'.", java);
        LOG.error(message, fromShowSettings.getException());
        throw new IllegalStateException(message, fromShowSettings.getException());
    }

    private static Result<JavaRuntimeProperties> fetchRuntimeProperties(String java, Function<String, ProcessBuilder> fetchPropsCommand) {
        try {
            final ProcessResult processResultShowSettings = runProcess(fetchPropsCommand.apply(java), 5, SECONDS);
            String errorOut = processResultShowSettings.getErrorOut();
            if (processResultShowSettings.wasSuccessful()) {
                final String standardOut = processResultShowSettings.getStandardOut();
                final JavaRuntimeProperties props = extractProperties(standardOut, errorOut);
                return Result.success(props);
            }
            final String command = fetchPropsCommand.apply(java).command().stream().collect(joining(" ", "'", "'"));
            LOG.debug("The command {} printed the following content to 'error out': {}", command, errorOut);
            throw new RuntimeException("Failed to execute command " + command);
        } catch (Exception e) {
            return Result.fail(e);
        }
    }

    private static ProcessBuilder showSettings(String java) {
        return new ProcessBuilder(java, SHOW_SETTINGS_ARG, VERSION_ARG);
    }

    private static ProcessBuilder showSystemProperties(String java) {
        final String openWebStartJar = OwsJvmLauncher.getOpenWebStartJar().getAbsolutePath();
        final String mainClass = SystemPropertiesPrinter.class.getName();
        return new ProcessBuilder(java, CP_ARG, openWebStartJar, mainClass);
    }

    private static JavaRuntimeProperties extractProperties(final String stdOut, final String stdErr) {
        final Map<String, String> props = new HashMap<>();
        props.putAll(extractRequiredProperties(stdErr));
        props.putAll(extractRequiredProperties(stdOut));

        if (props.size() != REQUIRED_PROPS.size()) {
            final String missing = REQUIRED_PROPS.stream().filter(prop -> !props.containsKey(prop)).collect(joining(","));
            final String msg = String.format("Could not find required properties %s.", missing);
            throw new RuntimeException(msg);
        }

        return new JavaRuntimeProperties(props);
    }

    private static Map<String, String> extractRequiredProperties(final String content) {
        return Stream.of(content.split("\\R"))
                .filter(line -> line.contains("="))
                .map(line -> line.split("=", 2))
                .map(parts -> new String[] {parts[0].trim(), parts[1].trim()})
                .filter(parts -> REQUIRED_PROPS.contains(parts[0]))
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }

    public static final class JavaRuntimeProperties {

        private final String vendor;
        private final String version;
        private final String osName;
        private final String osArch;
        private final String bitness;

        private JavaRuntimeProperties(Map<String, String> properties) {
            this.version = properties.get(JAVA_VERSION);
            this.vendor = properties.get(JAVA_VENDOR);
            this.osName = properties.get(OS_NAME);
            this.osArch = properties.get(OS_ARCH);
            this.bitness = properties.get(OS_BITNESS);
        }

        public String getVendor() {
            return vendor;
        }

        public String getVersion() {
            return version;
        }

        public String getOsName() {
            return osName;
        }

        public String getOsArch() {
            return osArch;
        }

        public String getBitness() {
            return bitness;
        }
    }
}
