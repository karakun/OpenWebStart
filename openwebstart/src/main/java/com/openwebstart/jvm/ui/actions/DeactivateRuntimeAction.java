package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.ui.BasicMutationAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.util.function.BiConsumer;

public class DeactivateRuntimeAction extends BasicMutationAction<LocalJavaRuntime> {

    public DeactivateRuntimeAction(final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> onChangeConsumer) {
        super(Translator.getInstance().translate("jvmManager.action.deactivateRuntime.text"), Translator.getInstance().translate("jvmManager.action.deactivateRuntime.description"), onChangeConsumer);
    }

    @Override
    protected LocalJavaRuntime mutate(final LocalJavaRuntime item) {
        return item.getDeactivatedCopy();
    }
}
