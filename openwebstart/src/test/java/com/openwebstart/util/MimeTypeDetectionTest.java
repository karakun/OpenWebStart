package com.openwebstart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

class MimeTypeDetectionTest {

    @Test
    void checkWrappedStreamForGeneralFunctionallity() throws IOException {
        //given
        final byte[] rawData = {0x50, 0x4b, 0x11, 0x3a, 0x4e};
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final InputStream inputStream = MimeTypeDetection.wrap(rawInputStream);

        //than
        Assertions.assertNotNull(inputStream);
        Assertions.assertEquals(0x50, inputStream.read());
        Assertions.assertEquals(0x4b, inputStream.read());
        Assertions.assertEquals(0x11, inputStream.read());
        Assertions.assertEquals(0x3a, inputStream.read());
        Assertions.assertEquals(0x4e, inputStream.read());
        Assertions.assertEquals(-1, inputStream.read());
    }

    @Test
    void checkWrappedStreamForPushbackFunctionallity() throws IOException {
        //given
        final byte[] rawData = {0x50, 0x4b, 0x11, 0x3a, 0x4e};
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final PushbackInputStream inputStream = MimeTypeDetection.wrap(rawInputStream);
        final byte[] readBytes = new byte[2];
        inputStream.read(readBytes, 0, 2);
        inputStream.unread(readBytes, 0, 2);

        //than
        Assertions.assertEquals(0x50, inputStream.read());
        Assertions.assertEquals(0x4b, inputStream.read());
    }

    @Test
    void checkForZipMimeTypeSupport() throws IOException {
        //given
        final byte[] rawData = {0x50, 0x4b, 0x11, 0x3a, 0x4e};
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final PushbackInputStream inputStream = MimeTypeDetection.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetection.getMimetype(inputStream);

        //than
        Assertions.assertEquals(MimeType.ZIP, mimeType);
        Assertions.assertEquals(0x50, inputStream.read());
        Assertions.assertEquals(0x4b, inputStream.read());
        Assertions.assertEquals(0x11, inputStream.read());

    }

    @Test
    void checkForNotSupportedMimeTypeSupport() throws IOException {
        //given
        final byte[] rawData = {0x51, 0x2b, 0x11, 0x3a, 0x4e};
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final PushbackInputStream inputStream = MimeTypeDetection.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetection.getMimetype(inputStream);

        //than
        Assertions.assertNull(mimeType);
        Assertions.assertEquals(0x51, inputStream.read());
        Assertions.assertEquals(0x2b, inputStream.read());
        Assertions.assertEquals(0x11, inputStream.read());

    }

    @Test
    void checkEmptyData() throws IOException {
        //given
        final byte[] rawData = new byte[0];
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final PushbackInputStream inputStream = MimeTypeDetection.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetection.getMimetype(inputStream);

        //than
        Assertions.assertNull(mimeType);
        Assertions.assertEquals(-1, inputStream.read());

    }
}