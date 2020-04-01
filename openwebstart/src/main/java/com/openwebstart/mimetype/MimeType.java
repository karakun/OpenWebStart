package com.openwebstart.mimetype;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static Optional<MimeType> getForMagicBytes(final byte[] data) {
        Assert.requireNonNull(data, "data");
        final Set<MimeType> matchingTypes = Arrays.asList(MimeType.values()).stream()
                .filter(m -> startWith(data, m.magicBytes))
                .collect(Collectors.toSet());
        if (matchingTypes.isEmpty()) {
            return Optional.empty();
        }
        if (matchingTypes.size() == 1) {
            return Optional.of(matchingTypes.iterator().next());
        }
        throw new IllegalStateException("More than 1 matching mimetype found!");
    }

    public static int getMaxMagicByteSize() {
        return Arrays.asList(MimeType.values()).stream()
                .mapToInt(m -> m.getMagicBytes().length)
                .max()
                .orElse(0);
    }

    private static boolean startWith(final byte[] data, final byte[] start) {
        if (start.length > data.length) {
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
