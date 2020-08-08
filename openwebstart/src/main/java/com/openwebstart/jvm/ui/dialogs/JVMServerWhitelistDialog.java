package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.ui.ModalDialog;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.AbstractServerWhitelistPanel;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;
import net.sourceforge.jnlp.util.whitelist.WhitelistEntry;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

import static com.openwebstart.config.OwsDefaultsProvider.JVM_SERVER_WHITELIST;

public class JVMServerWhitelistDialog extends ModalDialog {

    //private static final Logger LOG = LoggerFactory.getLogger(JVMServerWhitelistDialog.class);

    private final Translator translator = Translator.getInstance();
    private final JButton okButton;

    public JVMServerWhitelistDialog(final DeploymentConfiguration deploymentConfiguration) {
        setTitle(translator.translate("dialog.jvmManagerConfig.title"));

        final UiLock uiLock = new UiLock(deploymentConfiguration);
        okButton = new JButton(translator.translate("action.ok"));
        okButton.addActionListener(e -> {
            close();
        });

        final AbstractServerWhitelistPanel whitelistPanel= new AbstractServerWhitelistPanel(deploymentConfiguration) {
            @Override
            protected List<WhitelistEntry> createWhitelist() {
                return UrlWhiteListUtils.getUrlWhiteList(JVM_SERVER_WHITELIST);
            }
        };
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(8, 8));
        panel.add(whitelistPanel, BorderLayout.CENTER);
        panel.add(ButtonPanelFactory.createButtonPanel(okButton), BorderLayout.SOUTH);
        add(panel);
    }
}
