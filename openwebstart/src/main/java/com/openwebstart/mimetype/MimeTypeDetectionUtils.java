package com.openwebstart.mimetype;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class MimeTypeDetectionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MimeTypeDetectionUtils.class);

    private static final int PUSH_BACK_SIZE = MimeType.getMaxMagicByteSize();

    static MimeType getMimeType(final PushbackInputStream inputStream) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        final byte[] buffer = new byte[PUSH_BACK_SIZE];
        final int bytesRead = inputStream.read(buffer, 0, PUSH_BACK_SIZE);
        if (bytesRead > 0) {
            inputStream.unread(buffer, 0, bytesRead);
            LOG.debug("Magic bytes detection read: {}", printHumanReadable(buffer));
            return MimeType.getForMagicBytes(buffer).orElse(null);
        } else {
            LOG.error("Magic bytes can not be read!");
            return null;
        }
    }

    static PushbackInputStream wrap(final InputStream stream) {
        Assert.requireNonNull(stream, "stream");
        return new PushbackInputStream(stream, PUSH_BACK_SIZE);
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
