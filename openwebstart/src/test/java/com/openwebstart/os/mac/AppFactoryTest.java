package com.openwebstart.os.mac;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.openwebstart.jvm.os.OperationSystem;
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

	@Test
	public void testCreateApp() throws Exception {
		if (!OperationSystem.getLocalSystem().equals(OperationSystem.MAC64)) {
			return;
		}

		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assert new File(userHome).exists() == false;

		try {
			System.setProperty(JavaSystemPropertiesConstants.USER_HOME, userHome);
			assertTrue(userHome.equals(JavaSystemProperties.getUserHome()));
			updateCacheHome(userCache);
			assertTrue(userCache.equals(FilesystemConfiguration.getCacheHome()));

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
		} finally {
			restoreOrigins();
		}
	}

	@Test
	public void testDesktopLinkExists() throws Exception {
		if (!OperationSystem.getLocalSystem().equals(OperationSystem.MAC64)) {
			return;
		}

		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assertTrue(new File(userHome).exists() == false);

		final String appName = "MyFirstApp";
		final String appNameWithSuffix = appName + AppFactory.APP_EXTENSION;

		try {
			System.setProperty(JavaSystemPropertiesConstants.USER_HOME, userHome);
			assertTrue(userHome.equals(JavaSystemProperties.getUserHome()));
			updateCacheHome(userCache);
			assertTrue(userCache.equals(FilesystemConfiguration.getCacheHome()));

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
		} finally {
			restoreOrigins();
		}
	}

	@Test
	public void testCreateDesktopLink() throws Exception {
		if (!OperationSystem.getLocalSystem().equals(OperationSystem.MAC64)) {
			return;
		}

		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assertTrue(new File(userHome).exists() == false);

		final String appName = "MyFirstApp";
		final String appNameWithSuffix = appName + AppFactory.APP_EXTENSION;

		try {
			System.setProperty(JavaSystemPropertiesConstants.USER_HOME, userHome);
			assertTrue(userHome.equals(JavaSystemProperties.getUserHome()));
			updateCacheHome(userCache);
			assertTrue(userCache.equals(FilesystemConfiguration.getCacheHome()));

			final Path desktop = Paths.get(JavaSystemProperties.getUserHome(), "Desktop");
			Files.createDirectories(desktop);
			assertTrue(Files.isDirectory(desktop));

			final Path link = desktop.resolve(appNameWithSuffix);
			assertTrue(Files.exists(link) == false);

			AppFactory.createDesktopLink(appName, script, IcnsFactorySample.class.getResource("icon.png").getFile());

			assertTrue(Files.isSymbolicLink(link));
		} finally {
			restoreOrigins();
		}
	}

	private final static void restoreOrigins() throws Exception {
		System.setProperty(JavaSystemPropertiesConstants.USER_HOME, originUserHome);
		assert originUserHome.equals(JavaSystemProperties.getUserHome());
		updateCacheHome(originCacheHome);
		assert originCacheHome.equals(FilesystemConfiguration.getCacheHome());
	}

	@SuppressWarnings("unchecked")
	private static void updateCacheHome(final String val) throws ReflectiveOperationException {
		final Field field = FilesystemConfiguration.class.getDeclaredField("cacheHome");
		field.setAccessible(true);
		((AtomicReference<String>) field.get(null)).set(val);
	}
}
