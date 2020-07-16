package com.openwebstart.download;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApplicationDownloadDetailListModel extends AbstractListModel<ApplicationDownloadResourceState> {

    private final List<ApplicationDownloadResourceState> data = new ArrayList<>();

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public ApplicationDownloadResourceState getElementAt(final int index) {
        return data.get(index);
    }

    public void add(final ApplicationDownloadResourceState resourceState) {
        final ApplicationDownloadResourceState oldVersion = data.stream()
                .filter(v -> Objects.equals(v.getUrl(), resourceState.getUrl()))
                .filter(v -> Objects.equals(v.getVersion(), resourceState.getVersion()))
                .findAny()
                .orElse(null);
        if (oldVersion != null) {
            final int index = data.indexOf(oldVersion);
            data.set(index, resourceState);
            fireContentsChanged(this, index, index);
        } else {
            data.add(resourceState);
            fireIntervalAdded(this, data.size() - 1, data.size() - 1);
        }
    }

    public void remove(final ApplicationDownloadResourceState resourceState) {
        final ApplicationDownloadResourceState oldVersion = data.stream()
                .filter(v -> Objects.equals(v.getUrl(), resourceState.getUrl()))
                .filter(v -> Objects.equals(v.getVersion(), resourceState.getVersion()))
                .findAny()
                .orElse(null);
        if (oldVersion != null) {
            final int index = data.indexOf(oldVersion);
            data.remove(oldVersion);
            fireIntervalRemoved(this, index, index);
        }
    }
}
