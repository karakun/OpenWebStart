package com.openwebstart.os.linux;

import com.openwebstart.os.MenuAndDesktopEntriesFactory;
import com.openwebstart.os.ScriptFactory;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.XDesktopEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;

public class LinuxEntryFactory implements MenuAndDesktopEntriesFactory {

    private static final int ICON_SIZE = 32;

    private static final Logger LOG = LoggerFactory.getLogger(LinuxEntryFactory.class);

    @Override
    public void updateDesktopEntry(final JNLPFile file) throws IOException {

    }

    @Override
    public void updateMenuEntry(final JNLPFile file) throws IOException {

    }

    @Override
    public void createDesktopEntry(final JNLPFile file) throws IOException {
        final String iconLocation = getIconLocation(file);
        final File shortcutFile = getShortcutTmpFile(getDesktopIconName(file));

        if (!shortcutFile.getParentFile().isDirectory() && !shortcutFile.getParentFile().mkdirs()) {
            throw new IOException(shortcutFile.getParentFile().toString());
        }

        FileUtils.createRestrictedFile(shortcutFile);
        FileUtils.saveFileUtf8(getContent(file, false, iconLocation), shortcutFile);

        final String[] execString = new String[]{"xdg-desktop-icon", "install", "--novendor",
                shortcutFile.getCanonicalPath()};
        LOG.debug("Executing: {}", Arrays.toString(execString));
        final ProcessBuilder pb = new ProcessBuilder(execString);
        pb.inheritIO();
        final Process installer = pb.start();
        ProcessUtils.waitForSafely(installer);
        if (!shortcutFile.delete()) {
            throw new IOException("Unable to delete temporary file:" + shortcutFile);
        }
    }

    @Override
    public void createMenuEntry(final JNLPFile file) throws IOException {
        final File f = new File(findAndVerifyJavawsMenuDir() + "/" + createDesktopIconFileName(getDesktopIconName(file)));
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
        final XDesktopEntry desktopEntry = new XDesktopEntry(file);
        return desktopEntry.getLinuxMenuIconFile().exists();
    }

    private static String findAndVerifyJavawsMenuDir() {
        final File menuDir = PathsAndFiles.MENUS_DIR.getFile();
        if (!menuDir.exists()) {
            if (!menuDir.mkdirs()) {
                LOG.warn("directory '{}' for storing menu entry cannot be created.", menuDir);
            }
        }
        if(!menuDir.isDirectory()) {
            throw new IllegalStateException("Not a directory: " + menuDir.getAbsolutePath());
        }
        return menuDir.getAbsolutePath();
    }

    private static String createDesktopIconFileName(final String desktopIconName) {
        return desktopIconName + ".desktop";
    }

    private static String getDesktopIconName(JNLPFile file) {
        return sanitize(file.createNameForDesktopFile());
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
        File shortcutFile = new File(userTmp + File.separator + createDesktopIconFileName(desktopIconName));
        return shortcutFile;
    }

    private static String getContent(final JNLPFile file, boolean menu, final String iconLocation) {
        File generatedJnlp = null;

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
        String exec;
        exec = "Exec=" + ScriptFactory.createStartCommand(file) + "\"\n";
        fileContents += exec;
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
}
