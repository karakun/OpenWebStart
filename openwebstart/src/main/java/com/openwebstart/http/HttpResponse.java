package com.openwebstart.http;


import net.adoptopenjdk.icedteaweb.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class HttpResponse implements Closeable {

    private final HttpURLConnection connection;

    public HttpResponse(final HttpURLConnection connection) {
        this.connection = Assert.requireNonNull(connection, "connection");
    }

    private int readResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    private String getResponseMessage() throws IOException {
        return connection.getResponseMessage();
    }

    private HttpHeader getResponseHeader(final String name) {
        return getResponseHeaders().stream()
                .filter(h -> h.getName() != null)
                .filter(h -> h.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private List<HttpHeader> getResponseHeaders() {
        return connection.getHeaderFields().
                entrySet().
                stream().
                flatMap(e -> e.getValue().stream().map(v -> new HttpHeader(e.getKey(), v))).
                collect(Collectors.toList());
    }

    public long getContentSize() {
        return connection.getContentLengthLong();
    }

    public URL getConnectionUrl() {
        return connection.getURL();
    }

    public InputStream getContentStream() throws IOException {
        return ConnectionUtils.getContentStream(connection);
    }

    public void closeConnection() {
        connection.disconnect();
    }

    @Override
    public void close() {
        closeConnection();
    }
}
