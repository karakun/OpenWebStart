package com.openwebstart.jvm.util;

import com.openwebstart.jvm.json.RemoteRuntimeList;
import net.adoptopenjdk.icedteaweb.Assert;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RemoteRuntimeManagerCache implements Serializable {

    private final URL endpointForRequest;
    private final RemoteRuntimeList list;
    private final LocalDateTime endOfCache;

    public RemoteRuntimeManagerCache(final URL endpointForRequest, final RemoteRuntimeList list) {
        this.endpointForRequest = Assert.requireNonNull(endpointForRequest, "endpointForRequest");
        this.list = Assert.requireNonNull(list, "list");
        this.endOfCache = LocalDateTime.now().plus(list.getCacheTimeInMillis(), ChronoUnit.MILLIS);
    }

    public RemoteRuntimeList getList() {
        return list;
    }

    public URL getEndpointForRequest() {
        return endpointForRequest;
    }

    public boolean isStillValid() {
        return endOfCache.isAfter(LocalDateTime.now());
    }
}
