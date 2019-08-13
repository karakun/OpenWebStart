package com.openwebstart.jvm.io;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Collectors;


public class HttpRequest {

    private final HttpURLConnection connection;

    private final URI url;

    public HttpRequest(final URI url) throws IOException {
        this.url = Assert.requireNonNull(url, "url");

        final URLConnection connection = url.toURL().openConnection();
        if (connection instanceof HttpURLConnection) {
            this.connection = (HttpURLConnection) connection;
            this.connection.setRequestMethod("GET");
            connection.setUseCaches(false);
        } else {
            throw new RuntimeException("Not a HTTP connection");
        }
    }

    public void addRequestHeader(final String name, final String content) {
        addRequestHeader(new HttpHeader(name, content));
    }

    public void addRequestHeader(final HttpHeader headers) {
        Assert.requireNonNull(headers, "headers");
        connection.setRequestProperty(headers.getName(), headers.getContent());
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

    private long getContentSize() {
        return connection.getContentLengthLong();
    }

    private InputStream getContentStream() throws IOException {
        return ConnectionUtils.getContentStream(connection);
    }

    public HttpResponse handle() throws IOException {
        return new HttpResponse(getResponseHeaders(), readResponseCode(), getContentStream(), getContentSize());
    }

}
