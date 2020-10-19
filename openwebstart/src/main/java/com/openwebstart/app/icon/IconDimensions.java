package com.openwebstart.app.icon;

public enum IconDimensions {
    SIZE_512(512),
    SIZE_256(256),
    SIZE_128(128),
    SIZE_64(64),
    SIZE_32(32);

    private final int dimension;

    IconDimensions(final int dimension) {
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }
}
