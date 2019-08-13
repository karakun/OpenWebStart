package com.openwebstart.jvm.io;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static dev.rico.internal.core.RicoConstants.HASH_ALGORITHM;
import static dev.rico.internal.core.http.HttpHeaderConstants.CHARSET;

public class ConnectionUtils {

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    public static String toHex(final byte[] bytes) {
        return printHexBinary(bytes);
    }

    public static DigestInputStream createMD5HashStream(final InputStream inputStream) throws NoSuchAlgorithmException {
        Assert.requireNonNull(inputStream, "inputStream");
        final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        return new DigestInputStream(inputStream, digest);
    }


    public static String readUTF8Content(final InputStream inputStream) throws IOException {
        return new String(IOUtils.readContent(inputStream), CHARSET);
    }

    public static void writeUTF8Content(final OutputStream outputStream, final String content) throws IOException {
        Assert.requireNonNull(content, "content");
        IOUtils.writeContent(outputStream, content.getBytes(CHARSET));
    }
}
