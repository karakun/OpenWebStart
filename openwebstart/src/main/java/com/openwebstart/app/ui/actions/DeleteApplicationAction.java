package com.openwebstart.app.ui.actions;

import com.openwebstart.app.Application;
import com.openwebstart.ui.BasicAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;

import java.util.Optional;
import java.util.function.Consumer;

public class DeleteApplicationAction extends BasicAction<Application> {

    private Consumer<Application> afterDelete;

    public DeleteApplicationAction(Consumer<Application> afterDelete) {
        super(Translator.getInstance().translate("appManager.action.deleteApplication.text"), Translator.getInstance().translate("appManager.action.deleteApplication.description"));
        this.afterDelete = afterDelete;
    }

    @Override
    public void call(final Application item) {
        Cache.deleteFromCache(item.getId());
        Optional.ofNullable(afterDelete).ifPresent(c -> c.accept(item));
    }
}
