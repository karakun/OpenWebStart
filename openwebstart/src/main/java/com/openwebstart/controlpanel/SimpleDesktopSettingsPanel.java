package com.openwebstart.controlpanel;

import com.openwebstart.config.OwsDefaultsProvider;
import com.openwebstart.os.ShortcutUpdateStrategy;
import com.openwebstart.ui.TranslatableEnumComboboxRenderer;
import com.openwebstart.util.LayoutFactory;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ComboItem;
import net.adoptopenjdk.icedteaweb.client.util.UiLock;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;


public class SimpleDesktopSettingsPanel extends FormPanel {

    /**
     * Create a new instance of the desktop shortcut settings panel.
     *
     * @param config Loaded DeploymentConfiguration file.
     */
    public SimpleDesktopSettingsPanel(DeploymentConfiguration config) {
        final UiLock uiLock = new UiLock(config);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel description = new JLabel(Translator.getInstance().translate("desktop.integration.userInteractionType.name"));
        JComboBox<ComboItem> shortcutComboOptions = new JComboBox<>();
        ComboItem[] items = {deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_NEVER),
                deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ALWAYS),
                deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ASK_USER),
                deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ASK_USER_IF_HINTED),
                deploymentJavawsShortcutToComboItem(ShortcutDesc.CREATE_ALWAYS_IF_HINTED)};

        shortcutComboOptions.setActionCommand(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT); // The configuration property this combobox affects.
        for (int j = 0; j < items.length; j++) {
            shortcutComboOptions.addItem(items[j]);
            if (config.getProperty(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT).equals(items[j].getValue())) {
                shortcutComboOptions.setSelectedIndex(j);
            }
        }

        shortcutComboOptions.addItemListener(e -> {
            ComboItem c = (ComboItem) e.getItem();
            config.setProperty(shortcutComboOptions.getActionCommand(), c.getValue());
        });

        JLabel shortcutOverwriteDescription = new JLabel(Translator.getInstance().translate("desktop.integration.shortcutUpdateStrategy.name"));
        JComboBox<ShortcutUpdateStrategy> shortcutOverwriteComboOptions = new JComboBox<>(ShortcutUpdateStrategy.values());
        shortcutOverwriteComboOptions.setRenderer(new TranslatableEnumComboboxRenderer<>());
        shortcutOverwriteComboOptions.setActionCommand(OwsDefaultsProvider.SHORTCUT_UPDATE_STRATEGY); // The configuration property this combobox affects.
        shortcutOverwriteComboOptions.setSelectedItem(ShortcutUpdateStrategy.get(config.getProperty(OwsDefaultsProvider.SHORTCUT_UPDATE_STRATEGY)));
        shortcutOverwriteComboOptions.addItemListener(e -> {
            ShortcutUpdateStrategy c = (ShortcutUpdateStrategy) e.getItem();
            config.setProperty(shortcutOverwriteComboOptions.getActionCommand(), c.name());
        });

        final JLabel infoLabel = new JLabel();
        infoLabel.setIcon(new ImageIcon(this.getClass().getResource("/com/openwebstart/ui/impl/info-16.png")));
        infoLabel.setToolTipText(Translator.getInstance().translate("desktop.integration.shortcutUpdateStrategy.tooltip"));

        final JPanel panel = new JPanel();
        panel.setLayout(LayoutFactory.createBoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(shortcutOverwriteComboOptions);
        Dimension dim = new Dimension(10, -1);
        panel.add(new Box.Filler(dim, dim, dim));
        panel.add(infoLabel);

        uiLock.update(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT, shortcutComboOptions);
        uiLock.update(OwsDefaultsProvider.SHORTCUT_UPDATE_STRATEGY, shortcutOverwriteComboOptions);
        addRow(0, description, shortcutComboOptions);
        addRow(1, shortcutOverwriteDescription, panel);
        addFlexibleRow(2);
    }

    private static ComboItem deploymentJavawsShortcutToComboItem(String i) {
        return new ComboItem(ShortcutDesc.deploymentJavawsShortcutToString(i), i);
    }
}
