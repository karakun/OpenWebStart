package com.openwebstart.util;

@FunctionalInterface
public interface Subscription {

    /**
     * Unsubscribe / unregister the handling that is defined by the {@link Subscription} instance.
     */
    void unsubscribe();
}
