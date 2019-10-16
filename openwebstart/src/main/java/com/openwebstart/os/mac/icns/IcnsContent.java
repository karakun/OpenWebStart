package com.openwebstart.os.mac.icns;

public enum IcnsContent {

    ICON_16("icon_16x16.png", 16),
    ICON_16_RETINA("icon_16x16@2x.png", 32),
    ICON_32("icon_32x32.png", 32),
    ICON_32_RETINA("icon_32x32@2x.png", 64),
    ICON_128("icon_128x128.png", 128),
    ICON_128_RETINA("icon_128x128@2x.png", 256),
    ICON_256("icon_256x256.png", 256),
    ICON_256_RETINA("icon_256x256@2x.png", 512),
    ICON_512("icon_512x512.png", 512),
    ICON_512_RETINA("icon_512x512@2x.png", 1024);

    private final String fileName;

    private final int size;

    IcnsContent(final String fileName, final int size) {
        this.fileName = fileName;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public int getSize() {
        return size;
    }
}
