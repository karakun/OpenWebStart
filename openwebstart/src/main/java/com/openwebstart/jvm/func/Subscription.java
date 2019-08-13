package com.openwebstart.jvm.func;

@FunctionalInterface
public interface Subscription {

    /**
     * Unsusbscribe / unregister the handling that is defined by the {@link dev.rico.core.functional.Subscription} instance.
     */
    void unsubscribe();
}
