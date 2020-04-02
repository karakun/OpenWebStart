package com.openwebstart.mimetype;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import static net.adoptopenjdk.icedteaweb.Assert.requireNonNull;

public class MimeTypeInputStream extends FilterInputStream {

    private static final Logger LOG = LoggerFactory.getLogger(MimeTypeInputStream.class);

    private static final int PUSH_BACK_SIZE = MimeType.getMaxMagicByteSize();

    private final MimeType mimeType;

    public MimeTypeInputStream(final InputStream stream) throws IOException {
        super(new PushbackInputStream(requireNonNull(stream, "stream"), PUSH_BACK_SIZE));
        mimeType = getMimeType((PushbackInputStream) in);
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    private MimeType getMimeType(final PushbackInputStream stream) throws IOException {
        final byte[] buffer = new byte[PUSH_BACK_SIZE];
        final int bytesRead = stream.read(buffer, 0, PUSH_BACK_SIZE);
        if (bytesRead > 0) {
            stream.unread(buffer, 0, bytesRead);
            LOG.debug("Magic bytes detection read: {}", printHumanReadable(buffer, bytesRead));
            return MimeType.getForMagicBytes(buffer, bytesRead).orElse(null);
        } else {
            LOG.error("Magic bytes can not be read!");
            return null;
        }
    }

    private String printHumanReadable(final byte[] bytes, final int bytesRead) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < bytesRead; i++) {
            final byte b = bytes[i];
            sb.append(String.format("0x%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }
}
