package com.openwebstart.func;

/**
 * Extension for the {@link Result} interface that provides access to the input
 * of the function on that the result is based.
 *
 * @param <V> type of the input
 * @param <R> type of the output
 */
public interface ResultWithInput<V, R> extends Result<R> {

    /**
     * Returns the input of the based function
     *
     * @return the input of the based function
     */
    V getInput();
}
