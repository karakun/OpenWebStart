package com.openwebstart.app.icon;

import com.openwebstart.app.Application;
import com.openwebstart.func.Result;
import com.openwebstart.os.linux.FavIcon;
import com.openwebstart.ui.ImageUtils;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ApplicationIconDownloadUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationIconDownloadUtils.class);

    public static BufferedImage downloadIcon(final Application application, final IconDimensions dimension) {
        Assert.requireNonNull(application, "application");
        Assert.requireNonNull(dimension, "dimension");
        final BufferedImage loadedIcon =  downloadIcon(application.getJnlpFile(), IconKind.SHORTCUT, dimension)
                .orElseGet(() -> downloadIcon(application.getJnlpFile(), IconKind.DEFAULT, dimension)
                        .orElseGet(() -> downloadFavIcon(application.getJnlpFile())
                                .orElseThrow(() -> new RuntimeException("No icon found for app"))));
        return ImageUtils.resize(loadedIcon, dimension.getDimension(), dimension.getDimension());
    }

    private static Optional<BufferedImage> downloadIcon(final JNLPFile jnlpFile, final IconKind iconKind, final IconDimensions dimension) {
        return getIconUrl(jnlpFile, iconKind, dimension)
                .map(u -> {
                    try {
                        return downloadFromUrl(u);
                    } catch (final IOException e) {
                        LOG.warn("Error while downloading icon from url '" + u + "': " + e.getMessage());
                        return null;
                    }
                });
    }

    private static Optional<BufferedImage> downloadFavIcon(final JNLPFile jnlpFile) {
        Assert.requireNonNull(jnlpFile, "jnlpFile");

        return Optional.ofNullable(jnlpFile.getNotNullProbableCodeBase())
                .map(URL::getPath)
                .map(FavIcon::possibleFavIconLocations)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(l -> getFavIconUrlStreamForPath(jnlpFile, l))
                .map(Result.of(ApplicationIconDownloadUtils::downloadFromUrl))
                .filter(Result::isSuccessful)
                .findFirst()
                .map(Result::getResult);
    }

    private static Stream<? extends URL> getFavIconUrlStreamForPath(final JNLPFile jnlpFile, final String l) {
        final List<URL> urls = new ArrayList<>();
        try {
            urls.add(FavIcon.favUrl("/", l, jnlpFile));
        } catch (MalformedURLException ignore) {
        }
        try {
            urls.add(FavIcon.favUrl("\\", l, jnlpFile));
        } catch (MalformedURLException ignore) {
        }
        return urls.stream();
    }

    private static Optional<URL> getIconUrl(final JNLPFile jnlpFile, final IconKind iconKind, final IconDimensions dimension) {
        Assert.requireNonNull(iconKind, "iconKind");
        Assert.requireNonNull(dimension, "dimension");

        return Optional.ofNullable(jnlpFile)
                .map(JNLPFile::getInformation)
                .map(i -> i.getIconLocation(iconKind, dimension.getDimension(), dimension.getDimension()));
    }

    private static BufferedImage downloadFromUrl(final URL url) throws IOException {
        Assert.requireNonNull(url, "url");
        try (final InputStream inputStream = url.openStream()) {
            return ImageIO.read(inputStream);
        }
    }

}
