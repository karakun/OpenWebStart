package com.openwebstart.ui;

public interface Action<T> {

    boolean isActive();

    String getName();

    String getDescription();

    void call(T item);

}
