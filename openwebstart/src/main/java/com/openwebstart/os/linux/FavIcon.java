package com.openwebstart.os.linux;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FavIcon {

    private static final Logger LOG = LoggerFactory.getLogger(FavIcon.class);

    private static final String FAVICON = "favicon.ico";

    private final JNLPFile jnlpFile;

    public FavIcon(final JNLPFile file) {
        this.jnlpFile = Assert.requireNonNull(file, "jnlpFile");
    }

    public File download() {
        final List<String> possibleFavIconLocations = possibleFavIconLocations(jnlpFile.getNotNullProbableCodeBase().getPath());
        try {
            for (String path : possibleFavIconLocations) {
                URL favico = favUrl("/", path, jnlpFile);
                //JNLPFile.openURL(favico, null, UpdatePolicy.ALWAYS);
                //this MAY throw npe, if url (specified in jnlp) points to 404
                //the below works just fine
                File cacheFile = CacheUtil.downloadAndGetCacheFile(favico, null);
                if (cacheFile != null) {
                    return cacheFile;
                }
            }
            //the icon is much more likely to be found behind / then behind \/
            //So rather duplicating the code here, then wait double time if the icon will be at the start of the path
            for (String path : possibleFavIconLocations) {
                URL favico = favUrl("\\", path, jnlpFile);
                File cacheFile = CacheUtil.downloadAndGetCacheFile(favico, null);
                if (cacheFile != null) {
                    return cacheFile;
                }
            }
        } catch (Exception ex) {
            LOG.error("Can not download or find favicon");
        }
        return null;
    }

    public static List<String> possibleFavIconLocations(String path) {
        while (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        List<String> r = new ArrayList<>();
        do {
            r.add(path);
            int last = Math.max(path.lastIndexOf("\\"), path.lastIndexOf("/"));
            if (last >= 0) {
                path = path.substring(0, last);
            }
        } while (path.contains("/") || path.contains("\\"));
        if (!r.contains("")) {
            r.add("");
        }
        return r;
    }

    public static URL favUrl(final String delimiter, final String path, final JNLPFile file) throws MalformedURLException {
        final String separator = path.endsWith(delimiter) ? "" : delimiter;
        return new URL(
                file.getNotNullProbableCodeBase().getProtocol(),
                file.getNotNullProbableCodeBase().getHost(),
                file.getNotNullProbableCodeBase().getPort(),
                path + separator + FAVICON);
    }
}
