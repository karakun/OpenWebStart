package com.openwebstart.mimetype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MimeTypeTest {

    @Test
    void checkZipFormat() {
        //given
        final byte[] data = {0x50, 0x4b, 0x11, 0x3a, 0x4e};

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data, data.length).orElse(null);

        //than
        assertEquals(MimeType.ZIP, mimeType);
    }

    @Test
    void checkUnknownFormat() {
        //given
        final byte[] data = {0x22, 0x1b, 0x11, 0x3a, 0x4e};

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data, data.length).orElse(null);

        //than
        assertNull(mimeType);
    }

    @Test
    void checkNoData() {
        assertThrows(NullPointerException.class, () -> MimeType.getForMagicBytes(null, 0));
    }

    @Test
    void checkEmptyData() {
        //given
        final byte[] data = new byte[0];

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data, data.length).orElse(null);

        //than
        assertNull(mimeType);
    }

    @Test
    void checkOneByteData() {
        //given
        final byte[] data = {0x50};

        //when
        final MimeType mimeType = MimeType.getForMagicBytes(data, data.length).orElse(null);

        //than
        assertNull(mimeType);
    }

    @Test
    void checkMaxMagicByteSize() {
        assertEquals(5, MimeType.getMaxMagicByteSize());
    }
}
