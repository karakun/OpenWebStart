package com.openwebstart.mimetype;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class MimeTypeDetection {

    private static final Logger LOG = LoggerFactory.getLogger(MimeTypeDetection.class);

    private static final int PUSHBACK_SIZE = MimeType.getMaxMagicByteSize();

    public static MimeType getMimetype(final PushbackInputStream inputStream) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        final byte[] buffer = new byte[PUSHBACK_SIZE];
        final int bytesRead = inputStream.read(buffer, 0, PUSHBACK_SIZE);
        if (bytesRead > 0) {
            inputStream.unread(buffer, 0, bytesRead);
            LOG.debug("Magic bytes detection read: {}", printHumanReadable(buffer));
            return MimeType.getForMagicBytes(buffer).orElse(null);
        } else {
            LOG.error("Magic bytes can not be read!");
            return null;
        }
    }

    public static PushbackInputStream wrap(final InputStream stream) {
        Assert.requireNonNull(stream, "stream");
        return new PushbackInputStream(stream, PUSHBACK_SIZE);
    }

    private static String printHumanReadable(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }

}
