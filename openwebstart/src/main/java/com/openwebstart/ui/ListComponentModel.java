package com.openwebstart.ui;

import javax.swing.DefaultListModel;
import java.util.List;

public class ListComponentModel<T> extends DefaultListModel<T> {

    public void replaceData(final List<T> loadedData) {
        clear();
        loadedData.forEach(this::addElement);
    }

    public void replaceItem(final T oldValue, final T newValue) {
        final int index = indexOf(oldValue);
        remove(index);
        add(index, newValue);
    }
}
