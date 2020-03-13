package com.openwebstart.jvm;

import com.openwebstart.config.OwsDefaultsProvider;
import net.sourceforge.jnlp.config.PathsAndFiles;

public class PathAndFiles {
    public static final PathsAndFiles.ItwCacheFileDescriptor JVM_CACHE_DIR = new PathsAndFiles.ItwCacheFileDescriptor("jvm-cache", "FILEjvmcache") {
        @Override
        public String getPropertiesKey() {
            return OwsDefaultsProvider.JVM_CACHE_DIR;
        }
    };
}
