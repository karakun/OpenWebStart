package com.openwebstart.os.linux;

import com.openwebstart.os.MenuAndDesktopEntriesFactory;
import com.openwebstart.util.ProcessResult;
import com.openwebstart.util.ProcessUtil;
import net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.RestrictedFileUtils;
import net.sourceforge.jnlp.util.XDesktopEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;

public class LinuxEntryFactory implements MenuAndDesktopEntriesFactory {

    private static final int ICON_SIZE = 32;

    private static final Logger LOG = LoggerFactory.getLogger(LinuxEntryFactory.class);

    @Override
    public void updateDesktopEntry(final JNLPFile file) throws Exception {
        createDesktopEntry(file);
    }

    @Override
    public void updateMenuEntry(final JNLPFile file) throws IOException {
        createMenuEntry(file);
    }

    @Override
    public void createDesktopEntry(final JNLPFile file) throws Exception {
        final String iconLocation = getIconLocation(file);
        final File shortcutFile = getShortcutTmpFile(getDesktopIconName(file));

        if (!shortcutFile.getParentFile().isDirectory() && !shortcutFile.getParentFile().mkdirs()) {
            throw new IOException(shortcutFile.getParentFile().toString());
        }

        RestrictedFileUtils.createRestrictedFile(shortcutFile);
        FileUtils.saveFileUtf8(getContent(file, false, iconLocation), shortcutFile);

        final ProcessBuilder pb = new ProcessBuilder("xdg-desktop-icon", "install", "--novendor",
                shortcutFile.getCanonicalPath());

        final ProcessResult processResult = ProcessUtil.runProcess(pb, 5, TimeUnit.SECONDS);
        if (processResult.wasUnsuccessful()) {
            LOG.debug("The xdg-desktop-icon process printed the following content on the error out: {}", processResult.getErrorOut());
            throw new RuntimeException("failed to execute xdg-desktop-icon binary");
        }

        if (!shortcutFile.delete()) {
            throw new IOException("Unable to delete temporary file:" + shortcutFile);
        }
    }

    @Override
    public void createMenuEntry(final JNLPFile file) throws IOException {
        final File f = getMenuEntryFile(file);
        final String iconLocation = getIconLocation(file);
        FileUtils.saveFileUtf8(getContent(file, true, iconLocation), f);
        LOG.info("Menu item created: {}", f.getAbsolutePath());

    }

    @Override
    public boolean existsDesktopEntry(final JNLPFile file) {
        final XDesktopEntry desktopEntry = new XDesktopEntry(file);
        return desktopEntry.getDesktopIconFile().exists();
    }

    @Override
    public boolean existsMenuEntry(final JNLPFile file) {
        return getMenuEntryFile(file).exists();
    }

    private File getMenuEntryFile(JNLPFile file) {
        return new File(findAndVerifyJavawsMenuDir() + "/" + createDesktopIconFileName(getDesktopIconName(file)));
    }

    private static String findAndVerifyJavawsMenuDir() {
        final File menuDir = PathsAndFiles.MENUS_DIR.getFile().getParentFile();
        if (!menuDir.exists()) {
            if (!menuDir.mkdirs()) {
                LOG.warn("directory '{}' for storing menu entry cannot be created.", menuDir);
            }
        }
        if (!menuDir.isDirectory()) {
            throw new IllegalStateException("Not a directory: " + menuDir.getAbsolutePath());
        }
        return menuDir.getAbsolutePath();
    }

    private static String createDesktopIconFileName(final String desktopIconName) {
        return desktopIconName + ".desktop";
    }

    private static String getDesktopIconName(JNLPFile file) {
        return sanitize(file.getShortcutName());
    }

    private static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        /* key=value pairs must be a single line */
        input = FileUtils.sanitizeFileName(input, '-');
        //return first line or replace new lines by space?
        return input.split("\n")[0];
    }

    private static File getShortcutTmpFile(final String desktopIconName) {
        String userTmp = PathsAndFiles.TMP_DIR.getFullPath();
        return new File(userTmp + File.separator + createDesktopIconFileName(desktopIconName));
    }

    private static String getContent(final JNLPFile file, boolean menu, final String iconLocation) {
        String fileContents = "[Desktop Entry]\n";
        fileContents += "Version=1.0\n";
        fileContents += "Name=" + getDesktopIconName(file) + "\n";
        fileContents += "GenericName=Java Web Start Application\n";
        fileContents += "Comment=" + sanitize(file.getInformation().getDescription()) + "\n";

        if (menu) {
            //keeping the default category because of KDE
            String menuString = "Categories=Network;";
            if (file.getInformation().getShortcut() != null
                    && file.getInformation().getShortcut().getMenu() != null
                    && file.getInformation().getShortcut().getMenu().getSubMenu() != null
                    && !file.getInformation().getShortcut().getMenu().getSubMenu().trim().isEmpty()) {
                menuString += file.getInformation().getShortcut().getMenu().getSubMenu().trim() + ";";
            }
            menuString += "Java;Javaws;";
            fileContents += menuString + "\n";
        }

        fileContents += "Type=Application\n";
        if (iconLocation != null) {
            fileContents += "Icon=" + iconLocation + "\n";
        } else {
            fileContents += "Icon=" + JAVAWS + "\n";

        }

        if (file.getInformation().getVendor() != null) {
            fileContents += "X-Vendor=" + sanitize(file.getInformation().getVendor()) + "\n";
        }
        fileContents += "Exec=" + createStartScript(file) + "\n";
        return fileContents;
    }

    private static String getIconLocation(final JNLPFile file) throws IOException {
        final File target = getTargetFile();

        final URL iconLocation = Optional
                .ofNullable(file.getInformation().getIconLocation(IconKind.SHORTCUT, ICON_SIZE, ICON_SIZE))
                .orElseGet(() -> file.getInformation().getIconLocation(IconKind.DEFAULT, ICON_SIZE, ICON_SIZE));

        if (iconLocation != null) {
            try {
                final File cacheFile = CacheUtil.downloadAndGetCacheFile(iconLocation, null);
                Files.copy(cacheFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return target.getAbsolutePath();
            } catch (final Exception e) {
                LOG.debug("app icon can not be used - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        try {
            final FavIcon favIcon = new FavIcon(file);
            final File cacheFile = favIcon.download();
            Files.copy(cacheFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target.getAbsolutePath();
        } catch (final Exception e) {
            LOG.debug("Favicon can not be used - {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }

        try (final InputStream inputStream = LinuxEntryFactory.class.getResourceAsStream("default-icon.png")) {
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target.getAbsolutePath();
        }
    }

    private static File getTargetFile() {
        final File iconsDir = PathsAndFiles.ICONS_DIR.getFile();
        if (!iconsDir.isDirectory()) {
            final boolean created = iconsDir.mkdirs();
            if (!created) {
                throw new IllegalStateException("cannot create icons dir");
            }
        }

        final String targetName = UUID.randomUUID().toString();
        return new File(iconsDir, targetName);
    }

    private static String createStartScript(final JNLPFile jnlpFile) {
        final String executable = System.getProperty(JavaSystemPropertiesConstants.ITW_BIN_LOCATION);
        if (!Files.isExecutable(Paths.get(executable))) {
            throw new IllegalStateException("Can not find executable");
        }
        final URL jnlpLocation = Optional.ofNullable(jnlpFile.getSourceLocation()).orElse(jnlpFile.getFileLocation());
        return "\"" + executable + "\" \"" + jnlpLocation + "\"";
    }
}
