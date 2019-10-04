package com.openwebstart.jvm.ui.actions;

import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.util.function.BiConsumer;

public class ActivateRuntimeAction extends BasicMutationAction<LocalJavaRuntime> {

    public ActivateRuntimeAction(final BiConsumer<LocalJavaRuntime, LocalJavaRuntime> onChangeConsumer) {
        super(Translator.getInstance().translate("jvmManager.action.activateRuntime.text"), Translator.getInstance().translate("jvmManager.action.activateRuntime.description"), onChangeConsumer);
    }

    @Override
    protected LocalJavaRuntime mutate(final LocalJavaRuntime item) {
        return item.getActivatedCopy();
    }
}
