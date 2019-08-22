package com.openwebstart.func;


import net.adoptopenjdk.icedteaweb.Assert;

/**
 * Implementation of a {@link Result} that is based
 * on a not successfully executed function
 *
 * @param <T> type of the input
 * @param <R> type of the output
 */
public class Fail<T, R> implements ResultWithInput<T, R> {

    private final T input;

    private final Exception exception;

    public Fail(final T input, final Exception exception) {
        this.input = input;
        this.exception = Assert.requireNonNull(exception, "exception");
    }

    public Fail(final Exception exception) {
        this.input = null;
        this.exception = Assert.requireNonNull(exception, "exception");
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public T getInput() {
        return input;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public R getResult() {
        throw new IllegalStateException("No result since call failed!");
    }
}
