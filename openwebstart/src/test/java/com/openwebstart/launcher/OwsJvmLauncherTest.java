package com.openwebstart.launcher;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

class OwsJvmLauncherTest {

    private final LocalJavaRuntime runtimeDummy = new LocalJavaRuntime("1.2.3", OperationSystem.WIN64, "Karakun", Paths.get("some/dummy/path"), LocalDateTime.now(), true, true);

    @ParameterizedTest
    @MethodSource("testData")
    public void testJreInformationExtraction(final String jnlpFileName, final String expectedVersion, final String expectedVendor, final String expectedUrl) throws Exception {
        //given
        final URL jnlpUrl = OwsJvmLauncherTest.class.getResource(jnlpFileName);
        final JNLPFile file = new JNLPFileFactory().create(jnlpUrl);

        final VersionString expectedRuntimeVersion = VersionString.fromString(expectedVersion);
        final Vendor expectedRuntimeVendor = Vendor.fromStringOrAny(expectedVendor);
        final URL expectedRuntimeUrl = Optional.ofNullable(expectedUrl)
                .filter(u -> !StringUtils.isBlank(u))
                .map(u -> toURL(u))
                .orElse(null);

        final JavaRuntimeProvider provider = (version, vendor, url) -> {
            Assertions.assertEquals(expectedRuntimeVersion, version);
            Assertions.assertEquals(expectedRuntimeVendor, vendor);
            Assertions.assertEquals(expectedRuntimeUrl, url);
            return Optional.of(runtimeDummy);
        };

        final OwsJvmLauncher launcher = new OwsJvmLauncher(provider);

        //when
        final Optional<OwsJvmLauncher.RuntimeInfo> javaRuntime = launcher.getJavaRuntime(file);

        //no 'than' part since the assertations happens in the dummy JavaRuntimeProvider
    }

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("jnlp-1.jnlp", "1.8+", "*", null),
                Arguments.of("jnlp-2.jnlp", "1.8*", "*", null),
                Arguments.of("jnlp-3.jnlp", "1.8*", "Karakun", null),
                Arguments.of("jnlp-4.jnlp", "1.8*", "*", null),
                Arguments.of("jnlp-5.jnlp", "1.8*", "*", null),
                Arguments.of("jnlp-6.jnlp", "1.8*", "*", null),
                Arguments.of("jnlp-7.jnlp", "1.8*", "AnyVendor", "http://www.any-vendor.net/jvm"));
    }

    private static URL toURL(final String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can not create URL: " + urlString, e);
        }
    }

}