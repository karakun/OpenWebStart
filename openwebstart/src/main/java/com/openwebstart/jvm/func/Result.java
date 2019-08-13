package com.openwebstart.jvm.func;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<R> {

    /**
     * Returns the outcome /result of the based functional call or throws an {@link IllegalStateException} if the
     * function was aborted by an exception. Such behavior can easily be checked by calling {@link Result#isSuccessful()}
     * @return the outcome
     * @throws IllegalStateException the exception if the based function was not executed sucessfully
     */
    R getResult() throws IllegalStateException;

    /**
     * Returns true if the based function was executed successfully, otherwise false.
     * @return true if the based function was executed successfully, otherwise false.
     */
    boolean isSuccessful();

    /**
     * Returns false if the based function was executed successfully, otherwise true.
      @return false if the based function was executed successfully, otherwise true.
     */
    default boolean isFailed() {
        return !isSuccessful();
    }

    /**
     * Returns the exception of the based functional call or {@code null} if the
     * function was executed successfully. Such behavior can easily be checked by calling {@link Result#isSuccessful()}
     * @return the exception or {@code null}
     */
    Exception getException();

    /**
     * Returns a successful result with the given value
     * @param value the value of the result
     * @param <B> the type of the result
     * @return the successful result
     */
    static <B> Result<B> sucess(final B value) {
        return new Sucess<>(value);
    }

    /**
     * Returns a failed result with the given exception
     * @param e the exception of the result
     * @param <B> the type of the result
     * @return the failed result
     */
    static <B> Result<B> fail(final Exception e) {
        return new Fail<>(e);
    }

    /**
     * Wraps a given {@link CheckedFunction} in a {@link Function} that returns the {@link dev.rico.core.functional.Result} of the given {@link CheckedFunction}
     * @param function the function
     * @param <A> type of the input parameter
     * @param <B> type of the result
     * @return a {@link Function} that returns the {@link dev.rico.core.functional.Result} of the given {@link CheckedFunction}
     */
    static <A, B> Function<A, Result<B>> of(final CheckedFunction<A, B> function) {
        return (a) -> {
            try {
                final B result = function.apply(a);
                return new Sucess<>(result);
            } catch (Exception e) {
                return new Fail<>(e);
            }
        };
    }

    /**
     * Wraps a given {@link CheckedSupplier} in a {@link Supplier} that returns the {@link dev.rico.core.functional.Result} of the given {@link CheckedSupplier}
     * @param supplier the supplier
     * @param <B> type of the result
     * @return a {@link Supplier} that returns the {@link dev.rico.core.functional.Result} of the given {@link CheckedSupplier}
     */
    static <B> Supplier<Result<B>> of(final CheckedSupplier<B> supplier) {
        return () -> {
            try {
                final B result = supplier.get();
                return new Sucess<>(result);
            } catch (Exception e) {
                return new Fail<>(e);
            }
        };
    }

    /**
     * Wraps a given {@link CheckedFunction} in a {@link Function} that returns the {@link dev.rico.core.functional.Result} of the given {@link CheckedFunction}
     * @param function the function
     * @param <A> type of the input parameter
     * @param <B> type of the result
     * @return a {@link Function} that returns the {@link dev.rico.core.functional.Result} of the given {@link CheckedFunction}
     */
    static <A, B> Function<A, ResultWithInput<A, B>> withInput(final CheckedFunction<A, B> function) {
        return (a) -> {
            try {
                final B result = function.apply(a);
                return new Sucess<>(a, result);
            } catch (Exception e) {
                return new Fail<>(a, e);
            }
        };
    }

 }
