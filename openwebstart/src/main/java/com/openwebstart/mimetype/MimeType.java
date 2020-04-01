package com.openwebstart.mimetype;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;

public enum MimeType {

    //see https://en.wikipedia.org/wiki/List_of_file_signatures

    TAR(new byte[]{0x75, 0x73, 0x74, 0x61, 0x72}),
    ZIP(new byte[]{0x50, 0x4b}),
    GZIP(new byte[]{0x1f, (byte) 0x8b});

    private final byte[] magicBytes;

    MimeType(final byte[] magicBytes) {
        this.magicBytes = magicBytes;
    }

    public byte[] getMagicBytes() {
        return magicBytes;
    }

    public static Optional<MimeType> getForMagicBytes(final byte[] data, final int bytesInData) {
        Assert.requireNonNull(data, "data");
        final int bytesToCompare = min(max(0, bytesInData), data.length); // 0 <= bytesToCompare <= data.length
        final List<MimeType> matchingTypes = Arrays.stream(MimeType.values())
                .filter(m -> startWith(data, m.magicBytes, bytesToCompare))
                .collect(Collectors.toList());
        if (matchingTypes.isEmpty()) {
            return Optional.empty();
        }
        if (matchingTypes.size() == 1) {
            return Optional.of(matchingTypes.get(0));
        }
        throw new IllegalStateException("More than 1 matching MimeType found!");
    }

    static int getMaxMagicByteSize() {
        return Arrays.stream(MimeType.values())
                .mapToInt(m -> m.getMagicBytes().length)
                .max()
                .orElse(0);
    }

    private static boolean startWith(final byte[] data, final byte[] start, final int bytesToCompare) {
        if (start.length > bytesToCompare) {
            return false;
        }
        for (int i = 0; i < start.length; i++) {
            if (start[i] != data[i]) {
                return false;
            }
        }
        return true;
    }
}
