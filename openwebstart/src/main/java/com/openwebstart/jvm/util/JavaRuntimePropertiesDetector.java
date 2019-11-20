package com.openwebstart.jvm.util;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.Collections.unmodifiableSet;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VENDOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_NAME;

/**
 * Algorithm to extract the Java Runtime Properties by executing "java -XshowSettings:properties -version"
 */
public class JavaRuntimePropertiesDetector {

    private static final Logger LOG = LoggerFactory.getLogger(JavaRuntimePropertiesDetector.class);

    private static final Set<String> REQUIRED_PROPS = unmodifiableSet(new HashSet<>(asList(JAVA_VENDOR, JAVA_VERSION, OS_NAME, OS_ARCH)));

    private static final String SHOW_SETTINGS_ARG = "-XshowSettings:properties";

    private static final String VERSION_ARG = "-version";

    private static final Executor OUTPUT_READER_EXECUTOR = Executors.newCachedThreadPool();
    
    public static JavaRuntimeProperties getProperties(Path javaHome) {
        LOG.info("trying to get definiton of local JVM at '{}'", javaHome);
        final String java = JavaExecutableFinder.findJavaExecutable(javaHome);
        try {
            final Process p = new ProcessBuilder(java, SHOW_SETTINGS_ARG, VERSION_ARG).start();
            final OutputReader stdOutReader = new OutputReader(p.getInputStream());
            final OutputReader stdErrReader = new OutputReader(p.getErrorStream());
            OUTPUT_READER_EXECUTOR.execute(stdOutReader);
            OUTPUT_READER_EXECUTOR.execute(stdErrReader);
            ProcessUtils.waitForSafely(p);
            final int returnCode = p.exitValue();
            if (returnCode != 0) {
                final RuntimeException exception = new RuntimeException("failed to execute java binary");
                LOG.error("Executing local java instance '{}' to receive JVM definition failed!", exception);
                LOG.debug("The java process printed the following content on the error out: {}", stdErrReader.content);
                throw exception;
            }

            return extractProperties(stdOutReader.content, stdErrReader.content);
        } catch (IOException ex) {
            final String message = "Can not get properties for JVM in path '" + java + "'";
            LOG.error(message, ex);
            throw new IllegalStateException(message, ex);
        }
    }

    private static JavaRuntimeProperties extractProperties(String stdOut, String stdErr) throws IOException {
        final Map<String, String> props = new HashMap<>();
        props.putAll(extractProps(stdErr));
        props.putAll(extractProps(stdOut));
        if (props.size() != REQUIRED_PROPS.size()) {
            throw new RuntimeException("Could not find all required properties. Only found " + props);
        }
        return new JavaRuntimeProperties(
                props.get(JAVA_VENDOR),
                props.get(JAVA_VERSION),
                props.get(OS_NAME),
                props.get(OS_ARCH)
        );
    }

    private static Map<String, String> extractProps(String content) throws IOException {
        final Map<String, String> props = new HashMap<>();
        final BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("=")) {
                final String[] parts = line.split("=", 2);
                final String key = parts[0].trim().toLowerCase();
                if (REQUIRED_PROPS.contains(key)) {
                    props.put(key, parts[1].trim());
                }
            }
        }
        return props;
    }

    public static class JavaRuntimeProperties {
        private final String vendor;
        private final String version;
        private final String osName;
        private final String osArch;

        JavaRuntimeProperties(String vendor, String version, String osName, String osArch) {
            this.version = version;
            this.vendor = vendor;
            this.osName = osName;
            this.osArch = osArch;
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
    }

    private static class OutputReader implements Runnable {

        private final InputStream in;
        private String content;

        private OutputReader(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                content = IOUtils.readContentAsUtf8String(in);
            } catch (IOException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                throw new RuntimeException(e);
            }
        }
    }
}
