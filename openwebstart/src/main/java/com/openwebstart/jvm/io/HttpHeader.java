package com.openwebstart.jvm.io;

public class HttpHeader {

    private final String name;

    private final String content;

    public HttpHeader(final String name, final String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return name + ":" + content;
    }
}
