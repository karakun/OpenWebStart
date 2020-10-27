package com.openwebstart.os;

import com.openwebstart.controlpanel.ButtonPanelFactory;
import com.openwebstart.controlpanel.FormPanel;
import com.openwebstart.ui.ModalDialog;
import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class AskForEntriesDialog extends ModalDialog {

    private final JCheckBox menuCheckBox;

    private final JCheckBox desktopCheckBox;

    public AskForEntriesDialog(final String appName, final boolean askForMenu, final boolean askForDesktop) {

        menuCheckBox = new JCheckBox(Translator.getInstance().translate("shortcuts.dialog.menuEntry.text"));
        desktopCheckBox = new JCheckBox(Translator.getInstance().translate("shortcuts.dialog.desktopEntry.text"));

        final FormPanel formPanel = new FormPanel();
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        final JLabel textLabel = new JLabel(Translator.getInstance().translate("shortcuts.dialog.text", appName));

        int row = 0;

        formPanel.addEditorRow(row++, textLabel);
        if (askForMenu) {
            formPanel.addEditorRow(row++, menuCheckBox);
            menuCheckBox.setSelected(true);
        }
        if (askForDesktop) {
            formPanel.addEditorRow(row++, desktopCheckBox);
            desktopCheckBox.setSelected(true);
        }
        formPanel.addFlexibleRow(row++);

        final JButton okButton = new JButton(Translator.getInstance().translate("action.ok"));
        okButton.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
        });
        final JPanel buttonPanel = ButtonPanelFactory.createButtonPanel(okButton);

        final JPanel contentPane = new JPanel(LayoutFactory.createBorderLayout());
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);
    }

    public AskForEntriesDialogResult showAndWaitForResult() {
        showAndWait();
        return new AskForEntriesDialogResult(desktopCheckBox.isSelected(), menuCheckBox.isSelected());
    }
}
