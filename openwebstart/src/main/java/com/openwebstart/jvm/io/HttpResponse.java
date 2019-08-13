package com.openwebstart.jvm.io;


import net.adoptopenjdk.icedteaweb.Assert;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class HttpResponse {

    private final List<HttpHeader> headers;

    private final int statusCode;

    private final InputStream contentStream;

    private final long contentSize;

    public HttpResponse(final List<HttpHeader> headers, final int statusCode, final InputStream contentStream, final long contentSize) {
        this.headers = Collections.unmodifiableList(Assert.requireNonNull(headers, "headers"));
        this.statusCode = statusCode;
        this.contentStream = Assert.requireNonNull(contentStream, "contentStream");
        this.contentSize = contentSize;
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public long getContentSize() {
        return contentSize;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
