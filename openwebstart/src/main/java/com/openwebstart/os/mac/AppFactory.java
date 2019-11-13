package com.openwebstart.os.mac;

import com.openwebstart.os.mac.icns.IcnsFactory;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AppFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AppFactory.class);



    private final static String CONTENTS_FOLDER_NAME = "Contents";

    private final static String MAC_OS_FOLDER_NAME = "MacOS";

    private final static String RESOURCES_FOLDER_NAME = "Resources";

    private final static String INFO_PLIST_NAME = "Info.plist";

    private final static String INFO_PLIST_TEMPLATE_NAME = "Info.plist.template";

    private final static String APP_EXTENSION = ".app";

    private final static String SCRIPT_NAME = "start.sh";

    private final static String SCRIPT_NAME_PROPERTY = "${scriptName}";

    private final static String ICON_FILE_PROPERTY = "${iconFile}";

    private final static String ICON_FILE_NAME = "icons";

    private final static String ICON_FILE_EXTENSION = ".icns";

    private final static String APPLICATIONS_FOLDER = "/Applications";

    public static boolean exists(final String name) {
        Assert.requireNonBlank(name, "name");
        final Path applicationsFolder = Paths.get(APPLICATIONS_FOLDER);
        final File appPackage = new File(applicationsFolder.toFile(), name + APP_EXTENSION);
        return appPackage.exists();
    }

    public static void createApp(final String name, final String script, final String... iconPaths) throws Exception {
        Assert.requireNonBlank(name, "name");
        Assert.requireNonBlank(script, "script");

        LOG.info("Creating app '{}'", name);

        final Path applicationsFolder = Paths.get(APPLICATIONS_FOLDER);

        final File appPackage = new File(applicationsFolder.toFile(), name + APP_EXTENSION);
        if(appPackage.exists()) {
            throw new IllegalArgumentException("App with name " + name + " already exists!");
        }
        if(!appPackage.mkdirs()) {
            throw new IOException("Cannot create app directory");
        }
        LOG.debug("App '{}' will be placed at '{}'", name, appPackage);

        final File contentsFolder = new File(appPackage, CONTENTS_FOLDER_NAME);
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
    }

    private static InputStream getIcnsInputStream(final String... iconPaths) throws Exception {
        if(iconPaths == null || iconPaths.length == 0) {
            return AppFactory.class.getResourceAsStream("icons.icns");
        }
        final IcnsFactory factory = new IcnsFactory();
        final List<File> iconFiles = Arrays.asList(iconPaths).stream().map(v -> new File(v)).collect(Collectors.toList());
        return new FileInputStream(factory.createIconSet(iconFiles));
    }

}
