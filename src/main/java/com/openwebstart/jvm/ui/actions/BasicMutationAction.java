package com.openwebstart.jvm.ui.actions;


import net.adoptopenjdk.icedteaweb.Assert;

import java.util.function.BiConsumer;

public abstract class BasicMutationAction<T> extends BasicAction<T> {

    private final BiConsumer<T, T> onChangeConsumer;

    public BasicMutationAction(final String name, final String description, final BiConsumer<T, T> onChangeConsumer) {
        super(name, description);
        this.onChangeConsumer = Assert.requireNonNull(onChangeConsumer, "onChangeConsumer");
    }

    @Override
    public final void call(final T item) {
        final T newItem = mutate(item);
        onChangeConsumer.accept(item, newItem);

    }

    protected abstract T mutate(final T item);
}
