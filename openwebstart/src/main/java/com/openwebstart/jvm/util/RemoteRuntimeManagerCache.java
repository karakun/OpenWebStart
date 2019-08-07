package com.openwebstart.jvm.util;

import com.openwebstart.jvm.json.RemoteRuntimeList;
import net.adoptopenjdk.icedteaweb.Assert;

import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RemoteRuntimeManagerCache implements Serializable {

    private final URI endpointForRequest;

    private final LocalDateTime creationTime;

    private final RemoteRuntimeList list;

    public RemoteRuntimeManagerCache(final URI endpointForRequest, final RemoteRuntimeList list) {
        this(endpointForRequest, LocalDateTime.now(), list);
    }

    public RemoteRuntimeManagerCache(final URI endpointForRequest, final LocalDateTime creationTime, final RemoteRuntimeList list) {
        this.endpointForRequest = Assert.requireNonNull(endpointForRequest, "endpointForRequest");
        this.creationTime = Assert.requireNonNull(creationTime, "creationTime");
        this.list = Assert.requireNonNull(list, "list");
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public RemoteRuntimeList getList() {
        return list;
    }

    public URI getEndpointForRequest() {
        return endpointForRequest;
    }

    public boolean isStillValid() {
        final LocalDateTime endOfCache = creationTime.plus(list.getCacheTimeInMillis(), ChronoUnit.MILLIS);
        return endOfCache.isAfter(LocalDateTime.now());
    }
}
