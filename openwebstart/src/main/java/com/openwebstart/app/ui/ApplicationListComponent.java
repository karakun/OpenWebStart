package com.openwebstart.app.ui;

import com.openwebstart.app.Application;
import com.openwebstart.ui.Action;
import com.openwebstart.ui.ListComponent;

import java.util.List;
import java.util.function.Function;

public class ApplicationListComponent extends ListComponent<Application> {

    public ApplicationListComponent(final Function<Application, List<Action<Application>>> actionSupplier) {
        super(actionSupplier);
        setCellRenderer(new ApplicationListCellRenderer(getHighlighter()));
    }

}

