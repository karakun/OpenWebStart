package com.openwebstart.mimetype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class MimeTypeDetectionTest {

    @Test
    void checkWrappedStreamForGeneralFunctionallity() throws IOException {
        //given
        final byte[] rawData = {0x50, 0x4b, 0x11, 0x3a, 0x4e};
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final InputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);

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
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
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
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

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
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

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
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

        //than
        Assertions.assertNull(mimeType);
        Assertions.assertEquals(-1, inputStream.read());

    }

    @Test
    void checkForZipFile() throws IOException {
        //given
        final InputStream rawInputStream = new FileInputStream(MimeTypeDetectionTest.class.getResource("data.zip").getFile());

        //when
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

        //than
        Assertions.assertEquals(MimeType.ZIP, mimeType);
        final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        final ZipEntry entry = zipInputStream.getNextEntry();
        Assertions.assertNotNull(entry);
        Assertions.assertEquals("data.txt", entry.getName());
    }

    @Test
    void checkForGZipFile() throws IOException {
        //given
        final InputStream rawInputStream = new FileInputStream(MimeTypeDetectionTest.class.getResource("data.gz").getFile());

        //when
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

        //than
        Assertions.assertEquals(MimeType.GZIP, mimeType);
    }

    @Test
    void checkForTarGzFile() throws IOException {
        //given
        final InputStream rawInputStream = new FileInputStream(MimeTypeDetectionTest.class.getResource("data.tar.gz").getFile());

        //when
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

        //than
        Assertions.assertEquals(MimeType.GZIP, mimeType);
    }

    @Test
    void checkForUnsupportedFile() throws IOException {
        //given
        final InputStream rawInputStream = new FileInputStream(MimeTypeDetectionTest.class.getResource("data.txt").getFile());

        //when
        final PushbackInputStream inputStream = MimeTypeDetectionUtils.wrap(rawInputStream);
        final MimeType mimeType = MimeTypeDetectionUtils.getMimeType(inputStream);

        //than
        Assertions.assertNull(mimeType);
        Assertions.assertEquals(0x53, inputStream.read());
        Assertions.assertEquals(0x6f, inputStream.read());
    }
}