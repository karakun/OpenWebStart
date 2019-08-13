package com.openwebstart.jvm.func;

@FunctionalInterface
public interface CheckedSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception if the handling of the supplier throws an exception
     */
    T get() throws Exception;
}
