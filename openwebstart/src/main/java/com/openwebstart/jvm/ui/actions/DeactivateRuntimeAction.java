package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.util.function.BiConsumer;

public class DeactivateRuntimeAction extends BasicMutationAction<LocalJavaRuntime> {

    public DeactivateRuntimeAction(final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> onChangeConsumer) {
        super("deactivate JVM", "deactivate the JVM for usage as runtime for webstart applications", onChangeConsumer);
    }

    @Override
    protected LocalJavaRuntime mutate(final LocalJavaRuntime item) {
        return item.getDeactivatedCopy();
    }
}
