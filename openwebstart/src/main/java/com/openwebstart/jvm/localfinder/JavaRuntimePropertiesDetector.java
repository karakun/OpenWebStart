package com.openwebstart.jvm.localfinder;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VENDOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;

/**
 * Algorithm to extract the Java Runtime Properties by executing "java -XshowSettings:properties -version"
 */
public class JavaRuntimePropertiesDetector {

    private static final Logger LOG = LoggerFactory.getLogger(JavaRuntimePropertiesDetector.class);

    private static final Set<String> REQUIRED_PROPS = unmodifiableSet(new HashSet<>(asList(JAVA_VENDOR, JAVA_VERSION, OS_NAME, OS_ARCH)));

    public static JavaRuntimeProperties getProperties(Path javaHome) {
        final String java = JavaExecutableFinder.findJavaExecutable(javaHome);
        try {
            final Process p = new ProcessBuilder(java, "-XshowSettings:properties", "-version").start();
            ProcessUtils.waitForSafely(p);
            final String stdErr = IOUtils.readContentAsUtf8String(p.getErrorStream());
            final String stdOut = IOUtils.readContentAsUtf8String(p.getInputStream());
            final int returnCode = p.exitValue();
            if (returnCode != 0) {
                throw new RuntimeException("");
            }

            return extractProperties(stdOut, stdErr);
        } catch (IOException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            throw new RuntimeException(ex);
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
                props.get(JAVA_VENDOR), props.get(JAVA_VERSION),
                props.get(OS_NAME),
                props.get(OS_ARCH)
        );
    }

    private static Map<String, String> extractProps(String content) throws IOException {
        final Map<String, String> props = new HashMap<>();
        final BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while (!StringUtils.isBlank(line = reader.readLine())) {
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
}
