package com.openwebstart.jvm.func;

/**
 * Functional interface like {@link java.util.function.Function} that can throw a checked exception at runtime.
 *
 * @param <T> input parameter type
 * @param <R> output parameter type
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t argument of the function
     * @return result of the function
     * @throws Exception if the executing the function throws an exception
     */
    R apply(T t) throws Exception;
}
