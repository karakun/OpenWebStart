package com.openwebstart.jvm.func;

/**
 * Implementation of a {@link dev.rico.core.functional.Result} that is based
 * on a sucessfully executed function
 * @param <T> type of the input
 * @param <R> type of the output
 */
public class Sucess<T, R> implements ResultWithInput<T, R> {

    private final T input;

    private final R result;

    public Sucess(final T input, final R result) {
        this.input = input;
        this.result = result;
    }

    public Sucess(final R result) {
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
        return null;
    }

    @Override
    public R getResult() {
        return result;
    }
}

