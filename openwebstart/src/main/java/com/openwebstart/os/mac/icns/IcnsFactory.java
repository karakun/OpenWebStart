package com.openwebstart.os.mac.icns;

import com.openwebstart.func.Result;
import com.openwebstart.os.mac.AppFactory;
import com.openwebstart.ui.ImageUtils;
import com.openwebstart.util.ProcessResult;
import com.openwebstart.util.ProcessUtil;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IcnsFactory {

    private static final Logger LOG = LoggerFactory.getLogger(IcnsFactory.class);

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
                    LOG.warn("Handling icon file", v);
                    final BufferedImage image = ImageIO.read(v);
                    if (image == null) {
                        throw new IllegalStateException("Image '" + v + "' can not be read");
                    }
                    final int size = Math.min(image.getWidth(), image.getHeight());
                    return new ImageDefinition(v, size);
                }))
                .filter(r -> {
                    if (r.isFailed()) {
                        LOG.warn("Error in icon creation", r.getException());
                        return false;
                    }
                    return true;
                })
                .map(Result::getResult)
                .collect(Collectors.toList());

        Arrays.asList(IcnsContent.values()).forEach(i -> {
            Optional<ImageDefinition> imageDefinition = findBest(i, iconDefinitions);
            if (imageDefinition.isPresent()) {
                final ImageDefinition iconDefinition = imageDefinition.get();
                try {
                    final File imageFile = iconDefinition.getPath();
                    final BufferedImage image = ImageIO.read(imageFile);
                    final File iconFile = new File(iconFolder, i.getFileName());
                    LOG.debug("Will create icon file {}", iconFile);
                    if (image.getHeight() != i.getSize() || image.getWidth() != i.getSize()) {
                        final BufferedImage resizedImage = ImageUtils.resize(image, i.getSize(), i.getSize());
                        ImageIO.write(resizedImage, "png", iconFile);
                    } else {
                        ImageIO.write(image, "png", iconFile);
                    }
                } catch (final Exception e) {
                    LOG.error("Can not convert image to icon", e);
                }
            } else {
                LOG.warn("No ImageDefinition found for {}", i);
            }
        });

        if (iconFolder.list().length > 0) {
            LOG.debug("Will create icon file based on {} icons", iconFolder.list().length);

            final ProcessBuilder processBuilder = new ProcessBuilder(ICONUTIL_COMMAND);
            processBuilder.command(ICONUTIL_COMMAND);
            processBuilder.directory(tempDirectory.toFile());

            final ProcessResult processResult = ProcessUtil.runProcess(processBuilder, 5, TimeUnit.SECONDS);

            if (processResult.wasUnsuccessful()) {
                LOG.debug("The iconutil process printed the following content on the error out: {}", processResult.getErrorOut());
                throw new RuntimeException("failed to execute iconutil binary");
            }

            FileUtils.recursiveDelete(iconFolder, iconFolder);

            final File iconFile = new File(tempDirectory.toFile(), ICON_SET_NAME);
            if (!iconFile.exists()) {
                throw new RuntimeException("Error in creating icon file");
            }
            return iconFile;
        } else {
            LOG.debug("Will create icon file based on default icons");
            final File iconFile = new File(tempDirectory.toFile(), ICON_SET_NAME);
            try (FileOutputStream outputStream = new FileOutputStream(iconFile)) {
                IOUtils.copy(AppFactory.class.getResourceAsStream("icons.icns"), outputStream);
            }
            return iconFile;
        }


    }

    //TODO
    private Optional<ImageDefinition> findBest(final IcnsContent content, final List<ImageDefinition> definitions) {
        Assert.requireNonNull(content, "content");
        Assert.requireNonNull(definitions, "definitions");
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        Collections.sort(definitions);
        return Optional.ofNullable(definitions.get(0));
    }

}
