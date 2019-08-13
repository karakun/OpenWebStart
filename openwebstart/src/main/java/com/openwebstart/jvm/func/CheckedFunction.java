package com.openwebstart.jvm.func;

/**
 * Functional interface like {@link java.util.function.Function} that can throw an exception at runtime
 * @param <T> input parameter type
 * @param <R> output parameter type
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Method that handles the function
     * @param t input parameter
     * @return result of the function
     * @throws Exception if the handling of the function throws an exception
     */
    R apply(T t) throws Exception;

}
