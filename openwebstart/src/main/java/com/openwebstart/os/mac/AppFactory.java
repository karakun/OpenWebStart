package com.openwebstart.os.mac;

import com.openwebstart.os.mac.icns.IcnsFactory;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AppFactory {

    private final static String SCRIPT_START = "#!/bin/sh";

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

    public void createApp(final String name, final String... iconPaths) throws Exception {
        Assert.requireNonBlank(name, "name");

        final Path applicationsFolder = Paths.get(APPLICATIONS_FOLDER);

        final File appPackage = new File(applicationsFolder.toFile(), name + APP_EXTENSION);
        if(appPackage.exists()) {
            throw new IllegalArgumentException("App with name " + name + " already exists!");
        }
        if(!appPackage.mkdirs()) {
            throw new IOException("Cannot create app directory");
        }

        final File contentsFolder = new File(appPackage, CONTENTS_FOLDER_NAME);
        if(!contentsFolder.mkdirs()) {
            throw new IOException("Cannot create contents directory");
        }

        final File resourcesFolder = new File(contentsFolder, RESOURCES_FOLDER_NAME);
        if(!resourcesFolder.mkdirs()) {
            throw new IOException("Cannot create resources directory");
        }

        final File macFolder = new File(contentsFolder, MAC_OS_FOLDER_NAME);
        if(!macFolder.mkdirs()) {
            throw new IOException("Cannot create macOs directory");
        }

        final File iconsFile = new File(resourcesFolder, ICON_FILE_NAME + ICON_FILE_EXTENSION);
        try(final FileInputStream inputStream = new FileInputStream(getIcnsFile(iconPaths)); final FileOutputStream outputStream = new FileOutputStream(iconsFile)) {
            IOUtils.copy(inputStream, outputStream);
        }

        final File infoFile = new File(contentsFolder, INFO_PLIST_NAME);
        try(final FileInputStream inputStream = new FileInputStream(AppFactory.class.getResource(INFO_PLIST_TEMPLATE_NAME).getFile())) {
            final String infoContent = IOUtils.readContentAsUtf8String(inputStream)
                    .replaceAll(Pattern.quote(SCRIPT_NAME_PROPERTY), SCRIPT_NAME)
                    .replaceAll(Pattern.quote(ICON_FILE_PROPERTY), ICON_FILE_NAME);
            try(final FileOutputStream outputStream = new FileOutputStream(infoFile)) {
                IOUtils.writeUtf8Content(outputStream, infoContent);
            }
        }

        //TODO: Here we need to change the calculator sample to a concrete OpenWebStart call
        final File scriptFile = new File(macFolder, SCRIPT_NAME);
        final String scriptContent = SCRIPT_START + System.lineSeparator() + "open -a Calculator";
        try(final FileOutputStream outputStream = new FileOutputStream(scriptFile)) {
            IOUtils.writeUtf8Content(outputStream, scriptContent);
        }
        if(!scriptFile.setExecutable(true)) {
            throw new IOException("Cannot create script file");
        }
    }

    private File getIcnsFile(final String... iconPaths) throws Exception {
        if(iconPaths == null || iconPaths.length == 0) {
            return new File(AppFactory.class.getResource("icons.icns").getFile());
        }
        final IcnsFactory factory = new IcnsFactory();
        final List<File> iconFiles = Arrays.asList(iconPaths).stream().map(v -> new File(v)).collect(Collectors.toList());
        return factory.createIconSet(iconFiles);
    }

}
