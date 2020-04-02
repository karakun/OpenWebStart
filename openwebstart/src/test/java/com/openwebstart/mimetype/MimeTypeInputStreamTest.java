package com.openwebstart.mimetype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class MimeTypeInputStreamTest {

    @Test
    void checkForZipMimeTypeSupport() throws IOException {
        //given
        final byte[] rawData = {0x50, 0x4b, 0x11, 0x3a, 0x4e};
        final InputStream rawInputStream = new ByteArrayInputStream(rawData);

        //when
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

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
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

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
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

        //than
        Assertions.assertNull(mimeType);
        Assertions.assertEquals(-1, inputStream.read());

    }

    @Test
    void checkForZipFile() throws IOException {
        //given
        final InputStream rawInputStream = getClass().getResourceAsStream("data.zip");

        //when
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

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
        final InputStream rawInputStream = getClass().getResourceAsStream("data.gz");

        //when
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

        //than
        Assertions.assertEquals(MimeType.GZIP, mimeType);
    }

    @Test
    void checkForTarGzFile() throws IOException {
        //given
        final InputStream rawInputStream = getClass().getResourceAsStream("data.tar.gz");

        //when
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

        //than
        Assertions.assertEquals(MimeType.GZIP, mimeType);
    }

    @Test
    void checkForUnsupportedFile() throws IOException {
        //given
        final InputStream rawInputStream = getClass().getResourceAsStream("data.txt");

        //when
        final MimeTypeInputStream inputStream = new MimeTypeInputStream(rawInputStream);
        final MimeType mimeType = inputStream.getMimeType();

        //than
        Assertions.assertNull(mimeType);
        Assertions.assertEquals(0x53, inputStream.read());
        Assertions.assertEquals(0x6f, inputStream.read());
    }
}
