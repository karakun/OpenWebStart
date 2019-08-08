package com.openwebstart.jvm.ui.actions;

public interface Action<T> {

    boolean isActive();

    String getName();

    String getDescription();

    void call(T item);

}
