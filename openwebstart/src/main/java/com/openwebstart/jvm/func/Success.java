package com.openwebstart.jvm.func;

/**
 * Implementation of a {@link Result} that is based
 * on a successfully executed function
 *
 * @param <T> type of the input
 * @param <R> type of the output
 */
public class Success<T, R> implements ResultWithInput<T, R> {

    private final T input;

    private final R result;

    public Success(final T input, final R result) {
        this.input = input;
        this.result = result;
    }

    public Success(final R result) {
        this.input = null;
        this.result = result;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public T getInput() {
        return input;
    }

    @Override
    public Exception getException() {
        throw new IllegalStateException("No exception since call succeeded!");
    }

    @Override
    public R getResult() {
        return result;
    }
}

