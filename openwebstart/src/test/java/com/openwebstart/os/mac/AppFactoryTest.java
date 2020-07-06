package com.openwebstart.os.mac;

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

public final class AppFactoryTest
	extends Object
{
    private final static String SCRIPT_START = "#!/bin/sh";
    private final static String script = SCRIPT_START + System.lineSeparator() + "open -a Calculator";
    
	final static String originUserHome	= JavaSystemProperties.getUserHome();
	final static String originCacheHome	= FilesystemConfiguration.getCacheHome();

	private final static String userHome = "/tmp/dummy";
	private final static String userCache = "/tmp/dummy/.cache";
	
	
	@Test
	public void testCreateApp() 
		throws Exception
	{
		if ( !OperationSystem.getLocalSystem().equals(OperationSystem.MAC64) )
		{
			return;
		}

		FileUtils.recursiveDelete(new File(userHome), new File("/tmp"));
		assert new File(userHome).exists()==false;
		
		try
		{
	    	System.setProperty(JavaSystemPropertiesConstants.USER_HOME, userHome);
	    	assert userHome.equals(JavaSystemProperties.getUserHome());
	    	updateCacheHome(userCache);
	    	assert userCache.equals(FilesystemConfiguration.getCacheHome());
	    	
	    	final String appName = "MyFirstApp";
	    	
	    	final String appNameWithSuffix = appName + AppFactory.APP_EXTENSION;
	    	
	        AppFactory.createApp( appName, script, IcnsFactorySample.class.getResource("icon.png").getFile());
	        
	        assert Files.exists(Paths.get(userHome));
	        assert Files.exists(Paths.get(userCache));
	        assert Files.exists(Paths.get(userCache, "applications"));
	        assert Files.exists(Paths.get(userCache, "applications", appNameWithSuffix ));

	        assert Files.exists(Paths.get(userHome, "Applications"));
	        final Path link = Paths.get(userHome, "Applications", appNameWithSuffix);
	        assert Files.exists(link);
	        assert Files.isSymbolicLink(link);
	        assert link.toRealPath().equals(Paths.get(userCache, "applications", appNameWithSuffix).toRealPath());

	        // do it again
	        AppFactory.createApp( appName, script, IcnsFactorySample.class.getResource("icon.png").getFile());
		}
		finally
		{
			restoreOrigins();
		}
	}

	private final static void restoreOrigins()
		throws Exception
	{
    	System.setProperty(JavaSystemPropertiesConstants.USER_HOME, originUserHome);
    	assert originUserHome.equals(JavaSystemProperties.getUserHome());
    	updateCacheHome(originCacheHome);
    	assert originCacheHome.equals(FilesystemConfiguration.getCacheHome());
	}
	
	@SuppressWarnings("unchecked")
	private static void updateCacheHome( final String val ) 
		throws ReflectiveOperationException 
	{
		final Field field = FilesystemConfiguration.class.getDeclaredField("cacheHome");
		field.setAccessible(true);
		((AtomicReference<String>) field.get(null)).set(val);
	}
}
