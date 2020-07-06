package com.openwebstart.os.mac;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.openwebstart.os.mac.icns.IcnsFactorySample;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants;
import net.adoptopenjdk.icedteaweb.config.FilesystemConfiguration;
import net.adoptopenjdk.icedteaweb.io.FileUtils;

public final class AppFactoryTest extends Object {
	private final static String SCRIPT_START = "#!/bin/sh";
	private final static String script = SCRIPT_START + System.lineSeparator() + "open -a Calculator";

	final static String originUserHome = JavaSystemProperties.getUserHome();
	final static String originCacheHome = FilesystemConfiguration.getCacheHome();

	private final static String userHome = "/tmp/dummy";
	private final static String userCache = "/tmp/dummy/.cache";

	@BeforeEach
	public void beforeEach() throws Exception {
		System.setProperty(JavaSystemPropertiesConstants.USER_HOME, userHome);
		assertTrue(userHome.equals(JavaSystemProperties.getUserHome()));
		updateCacheHome(userCache);
		assertTrue(userCache.equals(FilesystemConfiguration.getCacheHome()));
	}

	@AfterAll
	static void restoreOrigins() throws Exception {
		System.setProperty(JavaSystemPropertiesConstants.USER_HOME, originUserHome);
		assert originUserHome.equals(JavaSystemProperties.getUserHome());
		updateCacheHome(originCacheHome);
		assert originCacheHome.equals(FilesystemConfiguration.getCacheHome());
	}
	
	@Test
	@EnabledOnOs(OS.MAC)
	public void testCreateApp() throws Exception {

		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assertTrue(new File(userHome).exists() == false);

		final String appName = "MyFirstApp";

		final String appNameWithSuffix = appName + AppFactory.APP_EXTENSION;

		AppFactory.createApp(appName, script, IcnsFactorySample.class.getResource("icon.png").getFile());

		assertTrue(Files.exists(Paths.get(userHome)));
		assertTrue(Files.exists(Paths.get(userCache)));
		assertTrue(Files.exists(Paths.get(userCache, "applications")));
		assertTrue(Files.exists(Paths.get(userCache, "applications", appNameWithSuffix)));

		assertTrue(Files.exists(Paths.get(userHome, "Applications")));
		final Path link = Paths.get(userHome, "Applications", appNameWithSuffix);
		assertTrue(Files.exists(link));
		assertTrue(Files.isSymbolicLink(link));
		assertTrue(link.toRealPath().equals(Paths.get(userCache, "applications", appNameWithSuffix).toRealPath()));

		// do it again
		AppFactory.createApp(appName, script, IcnsFactorySample.class.getResource("icon.png").getFile());
	}

	@Test
	@EnabledOnOs(OS.MAC)
	public void testDesktopLinkExists() throws Exception {
		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assertTrue(new File(userHome).exists() == false);

		final String appName = "MyFirstApp";
		final String appNameWithSuffix = appName + AppFactory.APP_EXTENSION;

		assertTrue(AppFactory.desktopLinkExists(appName) == false);

		final Path desktop = Paths.get(JavaSystemProperties.getUserHome(), "Desktop");
		Files.createDirectories(desktop);
		assertTrue(Files.isDirectory(desktop));

		final Path link = desktop.resolve(appNameWithSuffix);

		{ // regular file
			assertTrue(Files.exists(link) == false);
			Files.createFile(link);
			assertTrue(Files.exists(link) == true);
			assertTrue(AppFactory.desktopLinkExists(appName) == false);
			Files.delete(link);
		}

		{ // directory
			assertTrue(Files.exists(link) == false);
			Files.createDirectories(link);
			assertTrue(Files.isDirectory(link) == true);
			assertTrue(AppFactory.desktopLinkExists(appName) == false);
			Files.delete(link);
		}

		final Path appRoot = AppFactory.createAppWithoutMenuEntry(appName, appNameWithSuffix,
				IcnsFactorySample.class.getResource("icon.png").getFile()).toPath();

		{ // wrong link
			assertTrue(Files.exists(link) == false);
			Files.createSymbolicLink(link, appRoot.resolve(AppFactory.CONTENTS_FOLDER_NAME));
			assertTrue(Files.isSymbolicLink(link) == true);
			assertTrue(AppFactory.desktopLinkExists(appName) == false);
			Files.delete(link);
		}

		{ // wrong link
			assertTrue(Files.exists(link) == false);
			Files.createSymbolicLink(link, appRoot);
			assertTrue(Files.isSymbolicLink(link) == true);
			assertTrue(AppFactory.desktopLinkExists(appName) == true);
			Files.delete(link);
		}
	}

	@Test
	@EnabledOnOs(OS.MAC)
	public void testCreateDesktopLink() throws Exception {
		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assertTrue(new File(userHome).exists() == false);

		final String appName = "MyFirstApp";
		final String appNameWithSuffix = appName + AppFactory.APP_EXTENSION;

		final Path desktop = Paths.get(JavaSystemProperties.getUserHome(), "Desktop");
		Files.createDirectories(desktop);
		assertTrue(Files.isDirectory(desktop));

		final Path link = desktop.resolve(appNameWithSuffix);
		assertTrue(Files.exists(link) == false);

		AppFactory.createDesktopLink(appName, script, IcnsFactorySample.class.getResource("icon.png").getFile());

		assertTrue(Files.isSymbolicLink(link));
	}

	@SuppressWarnings("unchecked")
	private static void updateCacheHome(final String val) throws ReflectiveOperationException {
		final Field field = FilesystemConfiguration.class.getDeclaredField("cacheHome");
		field.setAccessible(true);
		((AtomicReference<String>) field.get(null)).set(val);
	}
}
