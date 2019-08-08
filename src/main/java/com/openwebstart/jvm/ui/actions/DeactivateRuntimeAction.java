package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.util.function.BiConsumer;

public class DeactivateRuntimeAction extends BasicMutationAction<LocalJavaRuntime> {

    public DeactivateRuntimeAction(final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> onChangeConsumer) {
        super("deactivate JVM", "deactive the JVM for usage as runtime for webstart applications", onChangeConsumer);
    }

    @Override
    protected LocalJavaRuntime mutate(final LocalJavaRuntime item) {
        return new LocalJavaRuntime(item.getVersion(), item.getOperationSystem(), item.getVendor(), item.getJavaHome(), item.getLastUsage(), false, item.isManaged());
    }
}
