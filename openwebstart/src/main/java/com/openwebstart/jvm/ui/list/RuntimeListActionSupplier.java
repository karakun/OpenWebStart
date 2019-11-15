package com.openwebstart.jvm.ui.list;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.ui.Action;
import com.openwebstart.jvm.ui.actions.ActivateRuntimeAction;
import com.openwebstart.jvm.ui.actions.DeactivateRuntimeAction;
import com.openwebstart.jvm.ui.actions.DeleteRuntimeAction;
import com.openwebstart.jvm.ui.actions.RemoveRuntimeAction;
import net.adoptopenjdk.icedteaweb.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RuntimeListActionSupplier implements Function<LocalJavaRuntime, List<Action<LocalJavaRuntime>>> {

    private final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> changedItemConsumer;

    public RuntimeListActionSupplier(final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> changedItemConsumer) {
        this.changedItemConsumer = Assert.requireNonNull(changedItemConsumer, "changedItemConsumer");
    }

    @Override
    public List<Action<LocalJavaRuntime>> apply(final LocalJavaRuntime localJavaRuntime) {
        final List<Action<LocalJavaRuntime>> list = new ArrayList<>();

        if (localJavaRuntime.isActive()) {
            list.add(new DeactivateRuntimeAction(changedItemConsumer));
        } else {
            list.add(new ActivateRuntimeAction(changedItemConsumer));
        }

        if (localJavaRuntime.isManaged()) {
            list.add(new DeleteRuntimeAction());
        } else {
            list.add(new RemoveRuntimeAction());
        }

        return list;
    }
}
