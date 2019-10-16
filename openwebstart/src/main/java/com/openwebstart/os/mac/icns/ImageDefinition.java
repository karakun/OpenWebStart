package com.openwebstart.os.mac.icns;

import java.io.File;

class ImageDefinition implements Comparable<ImageDefinition> {

    private final File path;

    private final int size;

    public ImageDefinition(final File path, final int size) {
        this.path = path;
        this.size = size;
    }

    public File getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int compareTo(final ImageDefinition o) {
        if (o == null) {
            return 1;
        }
        return Integer.valueOf(size).compareTo(o.size);
    }
}
