package com.openwebstart.rico.functional;

import dev.rico.core.functional.Result;
import dev.rico.internal.core.functional.Fail;
import dev.rico.internal.core.functional.Sucess;

import java.util.function.Supplier;

//TODO: see https://github.com/rico-projects/rico/pull/69/files
@FunctionalInterface
public interface CheckedSupplier<T> {

    T get() throws Exception;

    static <B> Supplier<Result<B>> of(final CheckedSupplier<B> supplier) {
        return () -> {
            try {
                final B result = supplier.get();
                return new Sucess<>(result);
            } catch (final Exception e) {
                return new Fail<>(e);
            }
        };
    }
}