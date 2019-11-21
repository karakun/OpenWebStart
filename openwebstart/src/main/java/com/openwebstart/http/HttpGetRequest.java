package com.openwebstart.http;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class HttpGetRequest {

    private final HttpURLConnection connection;

    public HttpGetRequest(final URL url) throws IOException {
        Assert.requireNonNull(url, "url");

        final URLConnection connection = url.openConnection();
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

    public HttpResponse handle() {
        return new HttpResponse(connection);
    }

}
