package com.openwebstart.controlpanel;

import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.ui.AppIcon;
import com.openwebstart.ui.OpenWebStartDialogHeader;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanelStyle;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.AboutPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.DebugSettingsPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.DesktopSettingsPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.JvmSettingsPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.NetworkSettingsPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.PolicySettingsPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.UnsignedAppletsTrustingListPanelProvider;

import javax.swing.JPanel;
import java.awt.Image;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenWebStartControlPanelStyle implements ControlPanelStyle {

    private final static Set<String> UNSUPPORTED_PANEL_NAMES = new HashSet<>(Arrays.asList(
            AboutPanelProvider.NAME,
            DesktopSettingsPanelProvider.NAME,
            PolicySettingsPanelProvider.NAME,
            UnsignedAppletsTrustingListPanelProvider.NAME,
            JvmSettingsPanelProvider.NAME,
            DebugSettingsPanelProvider.NAME,
            NetworkSettingsPanelProvider.NAME));

    @Override
    public boolean isPanelActive(final String panelName) {
        return !UNSUPPORTED_PANEL_NAMES.contains(panelName);
    }

    @Override
    public String getDialogTitle() {
        return "OpenWebStart" + Install4JUtils.applicationVersion().map(v -> " " + v).orElse("");
    }

    @Override
    public JPanel createHeader() {
        return new OpenWebStartDialogHeader();
    }

    @Override
    public List<? extends Image> getDialogIcons() {
        try {
            return AppIcon.getAllIcons();
        } catch (IOException e) {
            throw new RuntimeException("Can not load icons", e);
        }
    }
}
