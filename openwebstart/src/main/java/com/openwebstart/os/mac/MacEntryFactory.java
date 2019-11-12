package com.openwebstart.os.mac;

import com.openwebstart.func.Result;
import com.openwebstart.os.MenuAndDesktopEntriesFactory;
import com.openwebstart.os.ScriptFactory;
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
    public void updateDesktopEntry(final JNLPFile file) throws Exception {

    }

    @Override
    public void createDesktopEntry(final JNLPFile file) throws Exception {

    }

    @Override
    public boolean existsDesktopEntry(final JNLPFile file) {
        return false;
    }

    @Override
    public void updateMenuEntry(final JNLPFile file) throws Exception {
        // not implemented
    }

    @Override
    public void createMenuEntry(final JNLPFile file) throws Exception {
        final String name = file.createNameForDesktopFile();

        final String script = ScriptFactory.createStartCommand(file);

        final List<String> shortcutIconLocations = getIconLocations(file, IconKind.SHORTCUT);
        if(shortcutIconLocations.isEmpty()) {
            final List<String> defaultIconLocations = getIconLocations(file, IconKind.DEFAULT);
            AppFactory.createApp(name, script, defaultIconLocations.toArray(new String[0]));
        } else {
            AppFactory.createApp(name, script, shortcutIconLocations.toArray(new String[0]));
        }

    }

    @Override
    public boolean existsMenuEntry(final JNLPFile file) {
        return AppFactory.exists(file.createNameForDesktopFile());
    }

    private static File downloadIcon(final URL url) throws IOException {
        final String targetName = UUID.randomUUID().toString();
        final File target = new File(PathsAndFiles.ICONS_DIR.getFile(), targetName);
        PathsAndFiles.ICONS_DIR.getFile().mkdirs();
        try(final InputStream inputStream = url.openStream()) {
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target;
        }
    }

    private static List<String> getIconLocations(final JNLPFile file, final IconKind iconKind) throws IOException {
        Assert.requireNonNull(file, "file");
        Assert.requireNonNull(iconKind, "iconKind");

        return Optional.ofNullable(file.getInformation())
                .map(i -> i.getIcons(iconKind))
                .map(l -> Arrays.asList(l))
                .orElse(Collections.emptyList())
                .stream()
                .map(Result.of(l -> downloadIcon(l.getLocation()).getAbsolutePath()))
                .filter(r -> r.isSuccessful())
                .map(r -> r.getResult())
                .collect(Collectors.toList());
    }
}
