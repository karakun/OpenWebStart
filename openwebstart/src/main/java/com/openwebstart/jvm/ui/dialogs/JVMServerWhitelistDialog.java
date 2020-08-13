package com.openwebstart.jvm.ui.dialogs;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.jvm.RuntimeManagerConfig;
import com.openwebstart.ui.ModalDialog;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.AbstractUrlWhitelistPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.util.whitelist.WhitelistEntry;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

public class JVMServerWhitelistDialog extends ModalDialog {

    private final Translator translator = Translator.getInstance();

    public JVMServerWhitelistDialog(final DeploymentConfiguration deploymentConfiguration) {
        setTitle(translator.translate("dialog.jvmManagerWhitelist.title"));

        JButton closeButton = new JButton(translator.translate("action.close"));
        closeButton.addActionListener(e -> close());

        final AbstractUrlWhitelistPanel whitelistPanel= new AbstractUrlWhitelistPanel(deploymentConfiguration) {
            @Override
            protected List<WhitelistEntry> getUrlWhitelist() {
                return RuntimeManagerConfig.getJvmServerWhitelist();
            }
        };
        final JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 300));
        panel.setLayout(new BorderLayout(8, 8));
        panel.add(whitelistPanel, BorderLayout.CENTER);
        panel.add(ButtonPanelFactory.createButtonPanel(closeButton), BorderLayout.SOUTH);
        add(panel);
    }
}
