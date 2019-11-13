package com.openwebstart.app.ui.actions;

import com.openwebstart.app.Application;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.os.ScriptFactory;
import com.openwebstart.ui.BasicAction;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.JNLPFile;

public class StartApplicationAction extends BasicAction<Application> {

    public StartApplicationAction() {
        super(Translator.getInstance().translate("appManager.action.startApplication.text"), Translator.getInstance().translate("appManager.action.startApplication.description"));
    }

    @Override
    public void call(final Application item) {
        try {
            final JNLPFile jnlpFile = new JNLPFile(item.getJnlpFileUrl());
            final Process startProcess = ScriptFactory.createStartProcess(jnlpFile);
            startProcess.waitFor();
        } catch (final Exception e) {
            DialogFactory.showErrorDialog("Can not remove app", e);
        }
    }
}
