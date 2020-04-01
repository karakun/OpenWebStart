package com.openwebstart.mimetype;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class MimeTypeInputStream extends FilterInputStream {

    private final MimeType mimeType;

    public MimeTypeInputStream(final InputStream inputStream) throws IOException {
        super(MimeTypeDetectionUtils.wrap(inputStream));
        mimeType = MimeTypeDetectionUtils.getMimeType((PushbackInputStream) in);
    }

    public MimeType getMimeType() {
        return mimeType;
    }
}
