package com.openwebstart.jvm;

import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.json.RemoteRuntimeList;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.openwebstart.jvm.os.OperationSystem.ARM32;
import static com.openwebstart.jvm.os.OperationSystem.LINUX64;
import static com.openwebstart.jvm.os.OperationSystem.MAC64;
import static com.openwebstart.jvm.os.OperationSystem.WIN64;
import static com.openwebstart.jvm.runtimes.Vendor.ADOPT;
import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;
import static com.openwebstart.jvm.runtimes.Vendor.ORACLE;

public class RemoteRuntimeManagerTest {

    private static final VersionId VERSION_1_8_224 = VersionId.fromString("1.8.224");
    private static final VersionId VERSION_1_8_225 = VersionId.fromString("1.8.225");
    private static final VersionId VERSION_11_0_1 = VersionId.fromString("11.0.1");
    private static final VersionId VERSION_11_0_2 = VersionId.fromString("11.0.2");

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
    public void init() throws Exception {
        final List<RemoteJavaRuntime> runtimes = new CopyOnWriteArrayList<>();
        final URL theOneAndOnlyJdkZip = new URL("http://localhost:8090/jvms/jdk.zip");

        for (OperationSystem os : Arrays.asList(MAC64, WIN64, LINUX64)) {
            runtimes.add(new RemoteJavaRuntime("1.8.145", os, "adopt", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.220", os, "adopt", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.224", os, "adopt", theOneAndOnlyJdkZip));

            runtimes.add(new RemoteJavaRuntime("1.8.146", os, "oracle", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.221", os, "oracle", theOneAndOnlyJdkZip));
            runtimes.add(new RemoteJavaRuntime("1.8.225", os, "oracle", theOneAndOnlyJdkZip));

            runtimes.add(new RemoteJavaRuntime("11.0.1", os, "adopt", theOneAndOnlyJdkZip));

            runtimes.add(new RemoteJavaRuntime("11.0.2", os, "oracle", theOneAndOnlyJdkZip));
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

        RuntimeManagerConfig.setDefaultRemoteEndpoint(new URI("http://localhost:" + port + "/jvms"));
        RuntimeManagerConfig.setNonDefaultServerAllowed(true);
        RuntimeManagerConfig.setNonDefaultVendorsAllowed(true);
        RuntimeManagerConfig.setDefaultVendor(null);
        RuntimeManagerConfig.setSupportedVersionRange(null);
    }

    @AfterEach
    public void reset() {
        Spark.stop();
        Spark.awaitStop();

        RuntimeManagerConfig.setNonDefaultServerAllowed(true);
        RuntimeManagerConfig.setNonDefaultVendorsAllowed(true);
        RuntimeManagerConfig.setDefaultVendor(null);
        RuntimeManagerConfig.setSupportedVersionRange(null);
    }

    @Test
    public void testRemoteRuntime_1() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;
        final String vendor = ANY_VENDOR.getName();
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_1_8_225, runtime.getVersion());
        Assertions.assertEquals(ORACLE, runtime.getVendor());
        Assertions.assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_2() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;
        final String vendor = ANY_VENDOR.getName();
        final OperationSystem operationSystem = WIN64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_1_8_225, runtime.getVersion());
        Assertions.assertEquals(ORACLE, runtime.getVendor());
        Assertions.assertEquals(WIN64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_3() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;
        final String vendor = "adopt";
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_1_8_224, runtime.getVersion());
        Assertions.assertEquals(ADOPT, runtime.getVendor());
        Assertions.assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_4() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;
        final String vendor = ANY_VENDOR.getName();
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_11_0_2, runtime.getVersion());
        Assertions.assertEquals(ORACLE, runtime.getVendor());
        Assertions.assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_5() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;
        final String vendor = "adopt";
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_11_0_1, runtime.getVersion());
        Assertions.assertEquals(ADOPT, runtime.getVendor());
        Assertions.assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_6() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("20+");
        final URL specificServerEndpoint = null;
        final String vendor = ANY_VENDOR.getName();
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_7() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;
        final String vendor = "not_found";
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_8() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final URL specificServerEndpoint = null;
        final String vendor = ANY_VENDOR.getName();
        final OperationSystem operationSystem = ARM32;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_9() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = null;
        final String vendor = "oracle";
        final OperationSystem operationSystem = MAC64;

        //when
        RuntimeManagerConfig.setNonDefaultVendorsAllowed(false);
        RuntimeManagerConfig.setDefaultVendor("adopt");
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_1_8_224, runtime.getVersion());
        Assertions.assertEquals(ADOPT, runtime.getVendor());
        Assertions.assertEquals(MAC64, runtime.getOperationSystem());
    }

    @Test
    public void testRemoteRuntime_10() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = new URL("http://do.not.exists/error");
        final String vendor = "oracle";
        final OperationSystem operationSystem = MAC64;

        //when
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        // then
        Assertions.assertNull(runtime);
    }

    @Test
    public void testRemoteRuntime_11() throws Exception {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final URL specificServerEndpoint = new URL("http://do.not.exists/error");
        final String vendor = ANY_VENDOR.getName();
        final OperationSystem operationSystem = MAC64;

        //when
        RuntimeManagerConfig.setNonDefaultServerAllowed(false);
        final RemoteJavaRuntime runtime = RemoteRuntimeManager.getInstance().getBestRuntime(versionString, specificServerEndpoint, vendor, operationSystem).orElse(null);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals(VERSION_1_8_225, runtime.getVersion());
        Assertions.assertEquals(ORACLE, runtime.getVendor());
        Assertions.assertEquals(MAC64, runtime.getOperationSystem());
    }
}
