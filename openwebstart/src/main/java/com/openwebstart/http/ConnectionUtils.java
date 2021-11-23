package com.openwebstart.http;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    /**
     * Create a Hash stream based on the specified algorithm such as MD5, SHA-256
     * @param inputStream Stream to be hashed
     * @param algorithm hashing algorithm
     * @return hash stream
     * @throws NoSuchAlgorithmException
     */
    public static DigestInputStream createHashStream(final InputStream inputStream, final String algorithm) throws NoSuchAlgorithmException {
        Assert.requireNonNull(inputStream, "inputStream");
        final MessageDigest digest = MessageDigest.getInstance(algorithm);
        return new DigestInputStream(inputStream, digest);
    }

    public static InputStream getContentStream(final HttpURLConnection connection) throws IOException {
        Assert.requireNonNull(connection, "connection");
        final InputStream errorstream = connection.getErrorStream();
        if (errorstream == null) {
            return connection.getInputStream();
        } else {
            return errorstream;
        }
    }
}
