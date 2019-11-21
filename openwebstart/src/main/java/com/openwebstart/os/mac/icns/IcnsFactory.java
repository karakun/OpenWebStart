package com.openwebstart.os.mac.icns;

import com.openwebstart.func.Result;
import com.openwebstart.ui.ImageUtils;
import com.openwebstart.util.ProcessUtil;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IcnsFactory {

    private final static String ICON_FOLDER_NAME = "icons.iconset";

    private final static String ICON_SET_NAME = "icons.icns";

    private final static String[] ICONUTIL_COMMAND = {"iconutil", "-c", "icns", ICON_FOLDER_NAME};

    public File createIconSet(final List<File> icons) throws Exception {
        Assert.requireNonNull(icons, "icons");
        final Path tempDirectory = Files.createTempDirectory("icns");

        final File iconFolder = new File(tempDirectory.toFile(), ICON_FOLDER_NAME);
        if (!iconFolder.mkdirs()) {
            throw new IOException("Cannot create icon folder");
        }

        final List<ImageDefinition> iconDefinitions = icons.stream()
                .map(Result.of(v -> {
                    final BufferedImage image = ImageIO.read(v);
                    final int size = Math.min(image.getWidth(), image.getHeight());
                    return new ImageDefinition(v, size);
                }))
                .filter(r -> {
                    //TODO: Better handling
                    if (r.isFailed()) {
                        r.getException().printStackTrace();
                        return false;
                    }
                    return true;
                })
                .map(Result::getResult)
                .collect(Collectors.toList());

        Arrays.asList(IcnsContent.values()).forEach(i -> {
            try {
                final ImageDefinition iconDefinition = findBest(i, iconDefinitions);
                final File imageFile = iconDefinition.getPath();
                final BufferedImage image = ImageIO.read(imageFile);
                final File iconFile = new File(iconFolder, i.getFileName());
                if(image.getHeight() != i.getSize() || image.getWidth() != i.getSize()) {
                    final BufferedImage resizedImage = ImageUtils.resize(image, i.getSize(), i.getSize());
                    ImageIO.write(resizedImage, "png", iconFile);
                } else {
                    ImageIO.write(image, "png", iconFile);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });

        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(ICONUTIL_COMMAND);
        processBuilder.directory(tempDirectory.toFile());
        processBuilder.redirectError();
        final Process process = processBuilder.start();
        ProcessUtil.logIO(process.getInputStream());
        final int exitValue = process.waitFor();
        if(exitValue != 0) {
            throw new RuntimeException("Error in creating icon file");
        }

        FileUtils.recursiveDelete(iconFolder, iconFolder);

        final File iconFile = new File(tempDirectory.toFile(), ICON_SET_NAME);
        if(!iconFile.exists()) {
            throw new RuntimeException("Error in creating icon file");
        }
        return iconFile;
    }

    //TODO
    private ImageDefinition findBest(final IcnsContent content, final List<ImageDefinition> definitions) {
        Assert.requireNonNull(content, "content");
        Assert.requireNonNull(definitions, "definitions");
        Collections.sort(definitions);
        return definitions.get(0);
    }

}
