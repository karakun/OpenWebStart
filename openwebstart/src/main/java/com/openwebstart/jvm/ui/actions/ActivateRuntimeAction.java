package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.util.function.BiConsumer;

public class ActivateRuntimeAction extends BasicMutationAction<LocalJavaRuntime> {

    public ActivateRuntimeAction(final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> onChangeConsumer) {
        super("activate JVM", "active the JVM for usage as runtime for webstart applications", onChangeConsumer);
    }

    @Override
    protected LocalJavaRuntime mutate(final LocalJavaRuntime item) {
        return item.getActivatedCopy();
    }
}
