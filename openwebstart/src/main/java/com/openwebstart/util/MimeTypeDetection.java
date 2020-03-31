package com.openwebstart.util;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class MimeTypeDetection {

    private static final int PUSHBACK_SIZE = MimeType.getMaxMagicByteSize();

    public static MimeType getMimetype(final PushbackInputStream inputStream) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        final byte[] buffer = new byte[PUSHBACK_SIZE];
        final int bytesRead = inputStream.read(buffer, 0, PUSHBACK_SIZE);
        if (bytesRead > 0) {
            inputStream.unread(buffer, 0, bytesRead);
            return MimeType.getForMagicBytes(buffer).orElse(null);
        } else {
            throw new IOException("Magic Bytes can not be read!");
        }
    }

    public static PushbackInputStream wrap(final InputStream stream) {
        Assert.requireNonNull(stream, "stream");
        return new PushbackInputStream(stream, PUSHBACK_SIZE);
    }

}
