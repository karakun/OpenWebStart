package com.openwebstart.jvm;

import com.openwebstart.jvm.io.ConnectionUtils;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class LocalRuntimeManagerTest {

    @BeforeEach
    public void init() throws Exception {
        final URL cacheFolderUrl = LocalRuntimeManagerTest.class.getResource("test-cache");
        final Path cacheFolder = Paths.get(cacheFolderUrl.toURI());

        final File cacheConfigTemplateFile = new File(cacheFolder.toFile(), "cache.template.json");
        final File cacheConfigFile = new File(cacheFolder.toFile(), "cache.json");

        if(cacheConfigFile.exists()) {
            cacheConfigFile.delete();
        }

        final FileInputStream templateInputStream = new FileInputStream(cacheConfigTemplateFile);
        final String content = ConnectionUtils.readUTF8Content(templateInputStream);
        final String cacheConfig = content.replace("{CACHE_FOLDER}", cacheFolder.toUri().toString());
        final FileOutputStream cacheConfigFileOutputStream = new FileOutputStream(cacheConfigFile);
        ConnectionUtils.writeUTF8Content(cacheConfigFileOutputStream, cacheConfig);


        RuntimeManagerConfig.getInstance().setSpecificVendorEnabled(true);
        RuntimeManagerConfig.getInstance().setDefaultVendor(null);
        RuntimeManagerConfig.getInstance().setSupportedVersionRange(null);


        RuntimeManagerConfig.getInstance().setCachePath(cacheFolder);
        LocalRuntimeManager.getInstance().loadRuntimes();
    }

    @AfterEach
    public void reset() {
        RuntimeManagerConfig.getInstance().setSpecificVendorEnabled(false);
        RuntimeManagerConfig.getInstance().setDefaultVendor(null);
        RuntimeManagerConfig.getInstance().setSupportedVersionRange(null);
    }

    @Test
    public void checkBestRuntime_1() {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final String vendor = RuntimeManagerConstants.VENDOR_ANY;
        final OperationSystem os = OperationSystem.MAC64;

        //when
        final LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("1.8.220", runtime.getVersion());
        Assertions.assertEquals("AdoptOpenJDK", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_2() {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final String vendor = RuntimeManagerConstants.VENDOR_ANY;
        final OperationSystem os = OperationSystem.MAC64;

        //when
        final LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("11.0.1", runtime.getVersion());
        Assertions.assertEquals("AdoptOpenJDK", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_3() {
        //given
        final VersionString versionString = VersionString.fromString("1.8*");
        final String vendor = RuntimeManagerConstants.VENDOR_ANY;
        final OperationSystem os = OperationSystem.MAC64;

        //when
        LocalRuntimeManager.getInstance().getAll().stream()
                .filter(r -> Objects.equals(r.getVersion(), "1.8.220"))
                .forEach(r -> {
                    final LocalJavaRuntime modified = r.getDeactivatedCopy();
                    LocalRuntimeManager.getInstance().replace(r, modified);
                });
        final LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("1.8.219", runtime.getVersion());
        Assertions.assertEquals("Oracle", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_4() {
        //given
        VersionString versionString = VersionString.fromString("1.8*");
        String vendor = "oracle";
        OperationSystem os = OperationSystem.MAC64;

        //when
        LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("1.8.219", runtime.getVersion());
        Assertions.assertEquals("Oracle", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_5() {
        //given
        final VersionString versionString = VersionString.fromString("1.8+");
        final String vendor = RuntimeManagerConstants.VENDOR_ANY;
        final OperationSystem os = OperationSystem.MAC64;

        //when
        RuntimeManagerConfig.getInstance().setSupportedVersionRange(VersionString.fromString("1.8*"));
        final LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("1.8.220", runtime.getVersion());
        Assertions.assertEquals("AdoptOpenJDK", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_6() {
        //given
        VersionString versionString = VersionString.fromString("1.8*");
        String vendor = "oracle";
        OperationSystem os = OperationSystem.MAC64;

        //when
        RuntimeManagerConfig.getInstance().setDefaultVendor("adopt");
        RuntimeManagerConfig.getInstance().setSpecificVendorEnabled(false);
        LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("1.8.220", runtime.getVersion());
        Assertions.assertEquals("AdoptOpenJDK", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_7() {
        //given
        VersionString versionString = VersionString.fromString("1.8*");
        String vendor = RuntimeManagerConstants.VENDOR_ANY;
        OperationSystem os = OperationSystem.MAC64;

        //when
        RuntimeManagerConfig.getInstance().setDefaultVendor("oracle");
        RuntimeManagerConfig.getInstance().setSpecificVendorEnabled(false);
        LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNotNull(runtime);
        Assertions.assertEquals("1.8.219", runtime.getVersion());
        Assertions.assertEquals("Oracle", runtime.getVendor());
        Assertions.assertEquals(OperationSystem.MAC64, runtime.getOperationSystem());
        Assertions.assertTrue(runtime.isManaged());
        Assertions.assertTrue(runtime.isActive());
    }

    @Test
    public void checkBestRuntime_8() {
        //given
        VersionString versionString = VersionString.fromString("1.8*");
        String vendor = "not_found";
        OperationSystem os = OperationSystem.MAC64;

        //when
        LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void checkBestRuntime_9() {
        //given
        VersionString versionString = VersionString.fromString("1.8*");
        String vendor = RuntimeManagerConstants.VENDOR_ANY;
        OperationSystem os = OperationSystem.ARM32;

        //when
        LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNull(runtime);
    }

    @Test
    public void checkBestRuntime_10() {
        //given
        VersionString versionString = VersionString.fromString("20*");
        String vendor = RuntimeManagerConstants.VENDOR_ANY;
        OperationSystem os = OperationSystem.MAC64;

        //when
        LocalJavaRuntime runtime = LocalRuntimeManager.getInstance().getBestRuntime(versionString, vendor, os);

        //than
        Assertions.assertNull(runtime);
    }
}
