package com.openwebstart.http;

@FunctionalInterface
public interface Subscription {

    /**
     * Unsusbscribe / unregister the handling that is defined by the {@link Subscription} instance.
     */
    void unsubscribe();
}
