package com.openwebstart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MimeTypeTest {

    @Test
    void checkZipFormat() {
        //given
        final byte[] data = {0x50, 0x4b, 0x11, 0x3a, 0x4e};

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data).orElse(null);

        //than
        Assertions.assertEquals(MimeType.ZIP, mimeType);
    }

    @Test
    void checkUnknownFormat() {
        //given
        final byte[] data = {0x22, 0x1b, 0x11, 0x3a, 0x4e};

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data).orElse(null);

        //than
        Assertions.assertNull(mimeType);
    }

    @Test
    void checkNoData() {
        //given
        final byte[] data = null;

        //when
        try {
            MimeType.getForMagicBytes(data);
            Assertions.fail();
        } catch (final Exception e) {
        }
    }

    @Test
    void checkEmptyData() {
        //given
        final byte[] data = new byte[0];

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data).orElse(null);

        //than
        Assertions.assertNull(mimeType);
    }

    @Test
    void checkOneByteData() {
        //given
        final byte[] data = {0x50};

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data).orElse(null);

        //than
        Assertions.assertNull(mimeType);
    }

    @Test
    void checkMaxMagicByteSize() {
        Assertions.assertEquals(2, MimeType.getMaxMagicByteSize());
    }

}