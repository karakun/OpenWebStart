package com.openwebstart.jvm.io;

import net.adoptopenjdk.icedteaweb.Assert;

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
        return printHexBinary(bytes).toUpperCase();
    }

    public static DigestInputStream createMD5HashStream(final InputStream inputStream) throws NoSuchAlgorithmException {
        Assert.requireNonNull(inputStream, "inputStream");
        final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        return new DigestInputStream(inputStream, digest);
    }

    public static long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonNull(outputStream, "outputStream");

        final byte[] buffer = new byte[bufferSize];
        long finalLength = 0;
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
            finalLength = finalLength  + len;
        }
        return finalLength;
    }

    public static long copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        return copy(inputStream, outputStream, 1024);
    }

    public static byte[] readContent(final InputStream inputStream) throws IOException {
        Assert.requireNonNull(inputStream, "inputStream");
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copy(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static void writeContent(final OutputStream outputStream, final byte[] rawData) throws IOException {
        Assert.requireNonNull(outputStream, "outputStream");
        Assert.requireNonNull(rawData, "rawData");
        outputStream.write(rawData);
        outputStream.flush();
    }

    public static String readUTF8Content(final InputStream inputStream) throws IOException {
        return new String(readContent(inputStream), CHARSET);
    }

    public static void writeUTF8Content(final OutputStream outputStream, final String content) throws IOException {
        Assert.requireNonNull(content, "content");
        writeContent(outputStream, content.getBytes(CHARSET));
    }
}
