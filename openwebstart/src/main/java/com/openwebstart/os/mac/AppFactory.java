package com.openwebstart.os.mac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.openwebstart.os.mac.icns.IcnsFactory;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.config.FilesystemConfiguration;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

public class AppFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AppFactory.class);



    public  final static String CONTENTS_FOLDER_NAME = "Contents";

    private final static String MAC_OS_FOLDER_NAME = "MacOS";

    private final static String RESOURCES_FOLDER_NAME = "Resources";

    private final static String INFO_PLIST_NAME = "Info.plist";

    private final static String INFO_PLIST_TEMPLATE_NAME = "Info.plist.template";

    public  final static String APP_EXTENSION = ".app";

    private final static String SCRIPT_NAME = "start.sh";

    private final static String SCRIPT_NAME_PROPERTY = "${scriptName}";

    private final static String ICON_FILE_PROPERTY = "${iconFile}";

    private final static String ICON_FILE_NAME = "icons";

    private final static String ICON_FILE_EXTENSION = ".icns";

    public static boolean exists(final String name) {
        Assert.requireNonBlank(name, "name");
        final Path applicationsFolder = ensureUserApplicationFolder(); 
        final File appPackage = new File(applicationsFolder.toFile(), name + APP_EXTENSION);
        return appPackage.exists();
    }

    public static void createApp
    (
    	final String name, final String script, final String... iconPaths
    ) 
    	throws Exception 
    {
    	final File appPackage = createAppWithoutMenuEntry(name, script, iconPaths);
    	
        final Path linkFolder = ensureUserApplicationFolder();
        final Path appLinkPath = linkFolder.resolve(name + APP_EXTENSION);
        if ( Files.exists(appLinkPath, LinkOption.NOFOLLOW_LINKS) )
        {
        	Files.delete(appLinkPath);
        }
        Files.createSymbolicLink(appLinkPath, appPackage.toPath());
    }

    final static File createAppWithoutMenuEntry
    (
    	final String name, final String script, final String... iconPaths
    ) 
    	throws Exception 
    {
        Assert.requireNonBlank(name, "name");
        Assert.requireNonBlank(script, "script");

        LOG.info("Creating app '{}'", name);

        final Path applicationsFolder = ensureUserApplicationCacheFolder();
        final File appPackage = new File(applicationsFolder.toFile(), name + APP_EXTENSION);
        if( !appPackage.exists() ) 
        {
        	if(!appPackage.mkdirs()) 
        	{
        		throw new IOException("Cannot create app directory");
        	}
        }
        LOG.debug("App '{}' will be placed at '{}'", name, appPackage);

        final File contentsFolder = new File(appPackage, CONTENTS_FOLDER_NAME);
        FileUtils.recursiveDelete(contentsFolder, appPackage);
        if(!contentsFolder.mkdirs()) {
            throw new IOException("Cannot create contents directory");
        }
        LOG.debug("Folder '{}' for app '{}' created", CONTENTS_FOLDER_NAME, name);

        final File resourcesFolder = new File(contentsFolder, RESOURCES_FOLDER_NAME);
        if(!resourcesFolder.mkdirs()) {
            throw new IOException("Cannot create resources directory");
        }
        LOG.debug("Folder '{}' for app '{}' created", RESOURCES_FOLDER_NAME, name);

        final File macFolder = new File(contentsFolder, MAC_OS_FOLDER_NAME);
        if(!macFolder.mkdirs()) {
            throw new IOException("Cannot create macOs directory");
        }
        LOG.debug("Folder '{}' for app '{}' created", MAC_OS_FOLDER_NAME, name);

        final File iconsFile = new File(resourcesFolder, ICON_FILE_NAME + ICON_FILE_EXTENSION);
        try(final InputStream inputStream = getIcnsInputStream(iconPaths); final FileOutputStream outputStream = new FileOutputStream(iconsFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
        LOG.debug("Iconfile for app '{}' created", name);


        final File infoFile = new File(contentsFolder, INFO_PLIST_NAME);
        try(final InputStream inputStream = AppFactory.class.getResourceAsStream(INFO_PLIST_TEMPLATE_NAME)) {
            final String infoContent = IOUtils.readContentAsUtf8String(inputStream)
                    .replaceAll(Pattern.quote(SCRIPT_NAME_PROPERTY), SCRIPT_NAME)
                    .replaceAll(Pattern.quote(ICON_FILE_PROPERTY), ICON_FILE_NAME);
            try(final FileOutputStream outputStream = new FileOutputStream(infoFile)) {
                IOUtils.writeUtf8Content(outputStream, infoContent);
            }
        }
        LOG.debug("{} for app '{}' created", INFO_PLIST_NAME, name);

        //TODO: Here we need to change the calculator sample to a concrete OpenWebStart call
        final File scriptFile = new File(macFolder, SCRIPT_NAME);
        try(final FileOutputStream outputStream = new FileOutputStream(scriptFile)) {
            IOUtils.writeUtf8Content(outputStream, script);
        }
        if(!scriptFile.setExecutable(true)) {
            throw new IOException("Cannot create script file");
        }
        LOG.debug("Script for app '{}' created", name);
        return appPackage;
    }

    private static InputStream getIcnsInputStream(final String... iconPaths) throws Exception {
        if(iconPaths == null || iconPaths.length == 0) {
            return AppFactory.class.getResourceAsStream("icons.icns");
        }
        final IcnsFactory factory = new IcnsFactory();
        final List<File> iconFiles = Arrays.stream(iconPaths).map(File::new).collect(Collectors.toList());
        return new FileInputStream(factory.createIconSet(iconFiles));
    }

    private final static Path ensureUserApplicationFolder()
    {
    	final String userHome = JavaSystemProperties.getUserHome();
    	final File appFolder = new File( new File(userHome), "Applications");
    	if ( !appFolder.exists() )
    	{
    		appFolder.mkdir();
    	}	
    	return appFolder.toPath();
    }
    
    private final static Path ensureUserApplicationCacheFolder()
    {
    	final Path appcache = Paths.get(FilesystemConfiguration.getCacheHome(), "applications");
    	if ( !Files.isDirectory(appcache) )
    	{
    		try
    		{
    			Files.createDirectories(appcache);
    		}
    		catch( final IOException ioExc )
    		{
    			throw new RuntimeException("Could not create application cache directory [" + appcache + "]", ioExc); 
    		}
    	}
    	return appcache;
    }
    
    final static Path getApplicationRootInCache( final String name )
    {
    	return Paths.get(FilesystemConfiguration.getCacheHome(), "applications", name + APP_EXTENSION );
    }
    
    public final static boolean desktopLinkExists( final String appname )
    {
        Assert.requireNonBlank(appname, "appname");
        final Path cache = getApplicationRootInCache(appname);
        if ( Files.isDirectory(cache) )
        {
        	final Path link = getDesktopLink(appname);
        	if ( Files.isSymbolicLink(link) )
        	{
        		try
        		{
            		final Path linkRealPath = link.toRealPath();
            		return cache.toRealPath().equals(linkRealPath);
        		}
        		catch( final Exception e )
        		{
        			/* ignore this error */
        		}
        	}	
        }
        return false;
    }

    public final static void createDesktopLink
    (
       	final String appname, final String script, final String... iconPaths
    ) 
       	throws Exception 
    {
        Assert.requireNonBlank(appname, "appname");
        if ( !desktopLinkExists(appname) )
        {
            final Path approot = getApplicationRootInCache(appname);
            if ( !Files.isDirectory(approot) )
            {
            	createAppWithoutMenuEntry(appname, script, iconPaths);
            }
            final Path link = getDesktopLink(appname);
        	Files.deleteIfExists(link);
        	Files.createSymbolicLink(link, approot);
        }	
    }

    private final static Path getDesktopLink(final String appname)
    {
    	return Paths.get(JavaSystemProperties.getUserHome(), "Desktop", appname + APP_EXTENSION );
    }
}
