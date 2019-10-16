package com.openwebstart.os.mac.icns;

import com.openwebstart.func.Result;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
            throw new IOException("Can not create icon folder");
        }

        final List<ImageDefinition> iconDefinitions = icons.stream()
                .map(Result.of(v -> {
                    final BufferedImage image = ImageIO.read(v);
                    final int size = Math.min(image.getWidth(), image.getHeight());
                    return new ImageDefinition(v, size);
                }))
                .filter(r -> {
                    if (r.isFailed()) {
                        r.getException().printStackTrace();
                        return false;
                    }
                    return true;
                })
                .map(r -> r.getResult())
                .collect(Collectors.toList());

        Arrays.asList(IcnsContent.values()).forEach(i -> {
            try {
                final ImageDefinition iconDefinition = findBest(i, iconDefinitions);
                final File imageFile = iconDefinition.getPath();
                final BufferedImage image = ImageIO.read(imageFile);
                final File iconFile = new File(iconFolder, i.getFileName());
                if(image.getHeight() != i.getSize() || image.getWidth() != i.getSize()) {
                    final BufferedImage resizedImage = resize(image, i.getSize(), i.getSize());
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
        final int exitValue = processBuilder.start().waitFor();
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

    public static BufferedImage resize(final BufferedImage source, final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D bg = image.createGraphics();
        final double sx = (double) width / source.getWidth();
        final double sy = (double) height / source.getHeight();

        bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        bg.scale(sx, sy);
        bg.drawImage(source, 0, 0, null);
        bg.dispose();
        return image;
    }

}
