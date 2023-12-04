package com.openwebstart.jvm;

import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.json.RemoteRuntimeList;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.openwebstart.jvm.os.OperationSystem.ARM32;
import static com.openwebstart.jvm.os.OperationSystem.LINUX64;
import static com.openwebstart.jvm.os.OperationSystem.MAC64;
import static com.openwebstart.jvm.os.OperationSystem.WIN64;
import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;
import static com.openwebstart.jvm.runtimes.Vendor.ECLIPSE;
import static com.openwebstart.jvm.runtimes.Vendor.ORACLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RemoteRuntimeManagerTest {

    private static final VersionId VERSION_1_8_224 = VersionId.fromString("1.8.224");
    private static final VersionId VERSION_1_8_225 = VersionId.fromString("1.8.225");
    private static final VersionId VERSION_11_0_1 = VersionId.fromString("11.0.1");
    private static final VersionId VERSION_11_0_2 = VersionId.fromString("11.0.2");

    private static final String THE_ONE_AND_ONLY_JDK_ZIP = "http://localhost:8090/jvms/jdk.zip";

    private static int getFreePort() {
        final int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            freePort = socket.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return freePort;
    }

    @BeforeEach
    public void init(@TempDir Path cacheFolder) throws Exception {
        final List<RemoteJavaRuntime> runtimes = new CopyOnWriteArrayList<>();

        for (OperationSystem os : Arrays.asList(MAC64, WIN64, LINUX64)) {
            runtimes.add(new RemoteJavaRuntime("1.8.145", os, "Temurin", THE_ONE_AND_ONLY_JDK_ZIP));
            runtimes.add(new RemoteJavaRuntime("1.8.220", os, "Temurin", THE_ONE_AND_ONLY_JDK_ZIP));
            runtimes.add(new RemoteJavaRuntime("1.8.224", os, "Temurin", THE_ONE_AND_ONLY_JDK_ZIP));

            runtimes.add(new RemoteJavaRuntime("1.8.146", os, "oracle", THE_ONE_AND_ONLY_JDK_ZIP));
            runtimes.add(new RemoteJavaRuntime("1.8.221", os, "oracle", THE_ONE_AND_ONLY_JDK_ZIP));
            runtimes.add(new RemoteJavaRuntime("1.8.225", os, "oracle", THE_ONE_AND_ONLY_JDK_ZIP));

            runtimes.add(new RemoteJavaRuntime("11.0.1", os, "Temurin", THE_ONE_AND_ONLY_JDK_ZIP));

            runtimes.add(new RemoteJavaRuntime("11.0.2", os, "oracle", THE_ONE_AND_ONLY_JDK_ZIP));
        }

        final int port = getFreePort();
        Spark.port(port);
        Spark.get("/jvms", ((request, response) -> {
            try {
                final RemoteRuntimeList list = new RemoteRuntimeList(runtimes, 5_000);
                return JsonHandler.getInstance().toJson(list);
            } catch (final Exception e) {
                e.printStackTrace();
                throw e;
            }
        }));
        Spark.init();
        Spark.awaitInitialization();

        RuntimeManagerConfig.setCachePath(cacheFolder);
        RuntimeManagerConfig.setDefaultRemoteEndpoint(new URL("http://localhost:" + port + "/jvms"));
        RuntimeManagerConfig.setNonDefaultServerAllowed(true);
        RuntimeManagerConfig.setDefaultVendor(null);
        RuntimeManagerConfig.setSupportedVersionRange(null);

        LocalRuntimeManager.getInstance().loadRuntimes(new DeploymentConfiguration());
    }

    @AfterEach
    public void reset() {
        Spark.stop();
        Spark.awaitStop();

        RuntimeManagerConfig.setNonDefaultServerAllowed(true);
        RuntimeManagerConfig.setDefaultVendor(null);
        RuntimeManagerConfig.setSupportedVersionRange(null);
    }

    @Test
    public void testRemoteRuntime_1() {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ANY_VENDOR, MAC64).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        assertEquals(VERSION_1_8_225, runtime.getVersion());
        assertEquals(ORACLE, runtime.getVendor());
        assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_2() {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ANY_VENDOR, WIN64).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        assertEquals(VERSION_1_8_225, runtime.getVersion());
        assertEquals(ORACLE, runtime.getVendor());
        assertEquals(WIN64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_3() {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ECLIPSE, MAC64).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        assertEquals(VERSION_1_8_224, runtime.getVersion());
        assertEquals(ECLIPSE, runtime.getVendor());
        assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_4() {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ANY_VENDOR, MAC64).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        assertEquals(VERSION_11_0_2, runtime.getVersion());
        assertEquals(ORACLE, runtime.getVendor());
        assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_5() {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ECLIPSE, MAC64).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        assertEquals(VERSION_11_0_1, runtime.getVersion());
        assertEquals(ECLIPSE, runtime.getVendor());
        assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_6() {
        //given
        final VersionString versionString = VersionString.fromString("20+");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ANY_VENDOR, MAC64).orElse(null);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_7() {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;
        final Vendor vendor = Vendor.fromString("not_found");

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, MAC64).orElse(null);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_8() {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ANY_VENDOR, ARM32).orElse(null);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_10() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = new URL("http://do.not.exists/error");

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ORACLE, MAC64).orElse(null);

        // then
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_11() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = new URL("http://do.not.exists/error");

        //when
        RuntimeManagerConfig.setNonDefaultServerAllowed(false);
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, ANY_VENDOR, MAC64).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        assertEquals(VERSION_1_8_225, runtime.getVersion());
        assertEquals(ORACLE, runtime.getVendor());
        assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testParseRemoteRuntimeJson() throws IOException {
        // given
        final String json = getJvmJsonContent();

        // when
        RemoteRuntimeList result = RemoteRuntimeManager.getInstance().parseRemoteRuntimeJson(json);

        // then
        assertEquals(64, result.getRuntimes().size());
    }

    private String getJvmJsonContent() throws IOException {
        final String workingDir = System.getProperty("user.dir");
        final String relativeJvmJsonPath = "download-server/resources/jvms.json";
        final File file = new File(workingDir, relativeJvmJsonPath);

        if (file.exists()) {
            return FileUtils.loadFileAsUtf8String(file);
        }

        return FileUtils.loadFileAsUtf8String(new File(workingDir, "../" + relativeJvmJsonPath));
    }

    @Test
    public void testParseRemoteRuntimeJson_AdoptIsConvertedToEclipse() {
        // given
        final String json = "{\n" +
                "  \"cacheTimeInMillis\":5000,\n" +
                "  \"runtimes\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"version\":\"8.0.282\",\n" +
                "      \"vendor\":\"AdoptOpenJDK\",\n" +
                "      \"os\":\"LINUX64\",\n" +
                "      \"href\":\"" + THE_ONE_AND_ONLY_JDK_ZIP + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";

        // when
        RemoteRuntimeList result = RemoteRuntimeManager.getInstance().parseRemoteRuntimeJson(json);

        // then
        assertEquals(1, result.getRuntimes().size());
        assertEquals(result.getRuntimes().get(0).getVendor(), ECLIPSE);
    }

    @Test
    public void testParseRemoteRuntimeJson_BrokenVendor() {
        // given
        final String json = "{\n" +
                "  \"cacheTimeInMillis\":5000,\n" +
                "  \"runtimes\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"version\":\"8.0.282\",\n" +
                "      \"vendor\":\"AdoptOpenJDK\",\n" +
                "      \"os\":\"BROKEN\",\n" +
                "      \"href\":\"" + THE_ONE_AND_ONLY_JDK_ZIP + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";

        // when
        RemoteRuntimeList result = RemoteRuntimeManager.getInstance().parseRemoteRuntimeJson(json);

        // then
        assertEquals(0, result.getRuntimes().size());
    }

    @Test
    public void testParseRemoteRuntimeJson_1_6() {
        // given
        final String json = "{\n" +
                "  \"cacheTimeInMillis\":5000,\n" +
                "  \"runtimes\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"version\":\"8.0.282\",\n" +
                "      \"vendor\":\"AdoptOpenJDK\",\n" +
                "      \"os\":\"WIN64\",\n" +
                "      \"href\":\"" + THE_ONE_AND_ONLY_JDK_ZIP + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"runtimes_1.6\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"version\":\"8.0.282\",\n" +
                "      \"vendor\":\"AdoptOpenJDK\",\n" +
                "      \"os\":\"MACARM64\",\n" +
                "      \"href\":\"" + THE_ONE_AND_ONLY_JDK_ZIP + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n";

        // when
        RemoteRuntimeList result = RemoteRuntimeManager.getInstance().parseRemoteRuntimeJson(json);

        // then
        assertEquals(2, result.getRuntimes().size());
    }
}
