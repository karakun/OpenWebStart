package com.openwebstart.jvm.func;

/**
 * Functional interface like {@link java.util.function.Supplier} that can throw a checked exception at runtime.
 *
 * @param <T> input parameter type
 */
@FunctionalInterface
public interface CheckedSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception if executing the supplier throws an exception
     */
    T get() throws Exception;
}
