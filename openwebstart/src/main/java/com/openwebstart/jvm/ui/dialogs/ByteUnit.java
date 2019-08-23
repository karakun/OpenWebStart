package com.openwebstart.jvm.ui.dialogs;

import java.util.Objects;

enum ByteUnit {

    BYTE("B", "B", "byte", 0),
    KILOBYTE("KB", "KiB", "kilobyte", 1),
    MEGABYTE("MB", "MiB", "megabyte", 2),
    GIGABYTE("GB", "GiB", "gigabyte", 3),
    TERABYTE("TB", "TiB", "terabyte", 4),
    PETABYTE("PB", "PiB", "petabyte", 5);

    private final String decimalShortName;

    private final String binaryShortName;

    private final String defaultName;

    private final int exponent;

    ByteUnit(final String decimalShortName, final String binaryShortName, final String defaultName, final int exponent) {
        this.decimalShortName = decimalShortName;
        this.binaryShortName = binaryShortName;
        this.defaultName = defaultName;
        this.exponent = exponent;
    }

    public String getDecimalShortName() {
        return decimalShortName;
    }

    public String getBinaryShortName() {
        return binaryShortName;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public int getExponent() {
        return exponent;
    }

    public static ByteUnit findBestUnit(final long byteCount) {
        return findBestUnit(byteCount, true);
    }

    public static ByteUnit findBestUnit(final long byteCount, boolean binary) {
        final ByteUnit[] dictionary = { KILOBYTE, MEGABYTE, GIGABYTE, TERABYTE, PETABYTE};
        final int unit = binary ? 1024 : 1000;
        if (byteCount < unit) return BYTE;

        int exp = (int) (Math.log(byteCount) / Math.log(unit));
        return dictionary[Math.min(Math.max(0, exp-1), dictionary.length -1)];
    }

    public double convertBytesToUnit(final long byteCount) {
        return convertBytesToUnit(byteCount, true);
    }

    public double convertBytesToUnit(final long byteCount, boolean binary) {
        if(Objects.equals(this, ByteUnit.BYTE)) {
            return byteCount;
        }
        final int unit = binary ? 1024 : 1000;
        return byteCount / Math.pow(unit, getExponent());
    }
}
