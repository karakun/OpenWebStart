package com.openwebstart.jvm.ui.list;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.ui.Action;
import com.openwebstart.ui.ListComponent;
import com.openwebstart.ui.ListHighlighter;

import java.util.List;
import java.util.function.Function;

public class RuntimeListComponent extends ListComponent<LocalJavaRuntime> {

    public RuntimeListComponent(final Function<LocalJavaRuntime, List<Action<LocalJavaRuntime>>> actionSupplier) {
        super(actionSupplier);

        setCellRenderer(new RuntimeListCellRenderer(new ListHighlighter<>(this)));
    }
}
