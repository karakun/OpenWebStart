package com.openwebstart.extensionpoint;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.controlpanel.OpenWebStartControlPanelStyle;
import com.openwebstart.jvm.JavaRuntimeManager;
import com.openwebstart.jvm.ui.dialogs.DialogFactory;
import com.openwebstart.jvm.ui.dialogs.RuntimeDownloadDialog;
import com.openwebstart.launcher.JavaRuntimeProvider;
import com.openwebstart.launcher.OwsJvmLauncher;
import com.openwebstart.os.MenuAndDesktopEntryHandler;
import com.openwebstart.os.ShortcutUpdateStrategy;
import com.openwebstart.proxy.WebStartProxySelector;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanelStyle;
import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.extensionpoint.ExtensionPoint;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.MenuAndDesktopIntegration;

import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;

import static com.openwebstart.download.ApplicationDownloadIndicator.DOWNLOAD_INDICATOR;
import static com.openwebstart.os.ShortcutUpdateStrategy.UNIQUE_NAME;

/**
 * Extension point providing OWS specific implementations.
 */
public class OwsExtensionPoint implements ExtensionPoint {

    @Override
    public JvmLauncher createJvmLauncher(final DeploymentConfiguration configuration) {
        final JavaRuntimeProvider javaRuntimeProvider = JavaRuntimeManager.getJavaRuntimeProvider(
                RuntimeDownloadDialog::showDownloadDialog,
                DialogFactory::askForRuntimeUpdate,
                JNLPRuntime.getConfiguration()
        );

        return new OwsJvmLauncher(javaRuntimeProvider);
    }

    @Override
    public DownloadIndicator createDownloadIndicator(final DeploymentConfiguration configuration) {
        return DOWNLOAD_INDICATOR;
    }

    @Override
    public MenuAndDesktopIntegration createMenuAndDesktopIntegration(final DeploymentConfiguration configuration) {
        return new MenuAndDesktopEntryHandler();
    }

    @Override
    public ProxySelector createProxySelector(final DeploymentConfiguration configuration) {
        return new WebStartProxySelector(configuration);
    }

    @Override
    public ControlPanelStyle createControlPanelStyle(final DeploymentConfiguration configuration) {
        return new OpenWebStartControlPanelStyle();
    }

    @Override
    public List<String> getTranslationResources() {
        return Collections.singletonList("i18n");
    }

    @Override
    public String uniqueShortcutSuffix(JNLPFile jnlpFile) {
        final String value = JNLPRuntime.getConfiguration().getProperty(OwsDefaultsProvider.SHORTCUT_UPDATE_STRATEGY);
        if (ShortcutUpdateStrategy.get(value) == UNIQUE_NAME) {
            return "_" + Math.abs(jnlpFile.getSourceLocation().hashCode());
        } else {
            return ExtensionPoint.super.uniqueShortcutSuffix(jnlpFile);
        }
    }
}
