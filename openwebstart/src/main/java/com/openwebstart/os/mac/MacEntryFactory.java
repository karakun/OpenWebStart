package com.openwebstart.os.mac;

import com.openwebstart.func.Result;
import com.openwebstart.os.MenuAndDesktopEntriesFactory;
import com.openwebstart.os.ScriptFactory;
import com.openwebstart.os.linux.FavIcon;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.PathsAndFiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MacEntryFactory implements MenuAndDesktopEntriesFactory {

    @Override
    public boolean supportsDesktopEntry() {
        return false;
    }

    @Override
    public void updateDesktopEntry(final JNLPFile file) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public void createDesktopEntry(final JNLPFile file) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean existsDesktopEntry(final JNLPFile file) {
        return false;
    }

    @Override
    public void updateMenuEntry(final JNLPFile file)
    	throws Exception
    {
    	this.createMenuEntry(file);
    }

    @Override
    public void createMenuEntry(final JNLPFile file) throws Exception {
        final String name = file.getShortcutName();
        final String script = ScriptFactory.createStartScript(file);
        final String[] icons = getIcons(file);

        AppFactory.createApp(name, script, icons);
    }

    private String[] getIcons(JNLPFile file) throws IOException {
        final List<String> shortcutIconLocations = getIconLocations(file, IconKind.SHORTCUT);
        if (shortcutIconLocations.isEmpty()) {
            final List<String> defaultIconLocations = getIconLocations(file, IconKind.DEFAULT);
            if(defaultIconLocations.isEmpty()) {
             final FavIcon favIcon = new FavIcon(file);
             return Optional.ofNullable(favIcon.download())
                     .map(File::getAbsolutePath)
                     .map(Collections::singletonList)
                     .orElse(Collections.emptyList()).toArray(new String[0]);
            }
            return defaultIconLocations.toArray(new String[0]);
        }
        return shortcutIconLocations.toArray(new String[0]);
    }

    @Override
    public boolean existsMenuEntry(final JNLPFile file) {
        return AppFactory.exists(file.getShortcutName());
    }

    private static File downloadIcon(final URL url) throws IOException {
        final String targetName = UUID.randomUUID().toString();
        final File target = new File(PathsAndFiles.ICONS_DIR.getFile(), targetName);
        PathsAndFiles.ICONS_DIR.getFile().mkdirs();
        try (final InputStream inputStream = url.openStream()) {
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target;
        }
    }

    private static List<String> getIconLocations(final JNLPFile file, final IconKind iconKind) {
        Assert.requireNonNull(file, "file");
        Assert.requireNonNull(iconKind, "iconKind");

        return Optional.ofNullable(file.getInformation())
                .map(i -> i.getIcons(iconKind))
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .map(Result.of(l -> downloadIcon(l.getLocation()).getAbsolutePath()))
                .filter(Result::isSuccessful)
                .map(Result::getResult)
                .collect(Collectors.toList());
    }
}
