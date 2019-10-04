package com.openwebstart.launcher;

import com.openwebstart.jvm.ui.util.IconComponent;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanelStyle;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.JVMPanel;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.AboutPanelProvider;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.JvmSettingsPanelProvider;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.ImageResources;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.util.List;
import java.util.Objects;

public class OpenWebStartControlPanelStyle implements ControlPanelStyle {

    private final Logger LOG = LoggerFactory.getLogger(OpenWebStartControlPanelStyle.class);

    @Override
    public boolean isPanelActive(final String panelName) {
        if(Objects.equals(panelName, AboutPanelProvider.NAME)) {
            return false;
        }
        if(Objects.equals(panelName, JvmSettingsPanelProvider.NAME)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDialogTitle() {
        return "OpenWebStart";
    }

    @Override
    public JPanel createHeader() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(25, 48, 59));
        panel.setPreferredSize(new Dimension(400, 64));

        final JLabel logoLabel1 = new JLabel();
        try {
            logoLabel1.setFont(Font.createFont(Font.TRUETYPE_FONT, OpenWebStartControlPanelStyle.class.getResource("OpenSans-SemiBold.ttf").openStream()));
        } catch (final Exception e) {
            LOG.warn("Unable to load font", e);
        }
        logoLabel1.setFont(logoLabel1.getFont().deriveFont(30.0f));
        logoLabel1.setText("Open");
        logoLabel1.setForeground(new Color(129, 198, 118));
        logoLabel1.setBackground(null);

        final JLabel logoLabel2 = new JLabel();
        try {
            logoLabel2.setFont(Font.createFont(Font.TRUETYPE_FONT, OpenWebStartControlPanelStyle.class.getResourceAsStream("OpenSans-Light.ttf")));
        } catch (final Exception e) {
            LOG.warn("Unable to load font", e);
        }
        logoLabel2.setFont(logoLabel2.getFont().deriveFont(30.0f));
        logoLabel2.setText("WebStart");
        logoLabel2.setForeground(Color.WHITE);
        logoLabel2.setBackground(null);

        final JLabel logoLabel3 = new JLabel();
        try {
            logoLabel3.setFont(Font.createFont(Font.TRUETYPE_FONT, OpenWebStartControlPanelStyle.class.getResourceAsStream("OpenSans-Light.ttf")));
        } catch (final Exception e) {
            LOG.warn("Unable to load font", e);
        }
        logoLabel3.setFont(logoLabel3.getFont().deriveFont(11.0f));
        logoLabel3.setText("powered by IcedTeaWeb");
        logoLabel3.setForeground(Color.WHITE);
        logoLabel3.setBackground(null);


        final JPanel logoPanel = new JPanel();
        logoPanel.setLayout(null);
        logoPanel.setBackground(null);

        logoLabel1.setSize(new Dimension(logoLabel1.getPreferredSize().width + 2, logoLabel1.getPreferredSize().height + 2));
        logoLabel2.setSize(new Dimension(logoLabel2.getPreferredSize().width + 2, logoLabel2.getPreferredSize().height + 2));
        logoLabel3.setSize(new Dimension(logoLabel3.getPreferredSize().width + 2, logoLabel3.getPreferredSize().height + 2));

        logoLabel1.setLocation(6, 2);
        logoPanel.add(logoLabel1);

        logoLabel2.setLocation(84, 2);
        logoPanel.add(logoLabel2);

        logoLabel3.setLocation(86, 34);
        logoPanel.add(logoLabel3);

        logoPanel.setPreferredSize(new Dimension(300, 64));

        try {
            final ImageIcon icon = new ImageIcon(ImageIO.read(OpenWebStartControlPanelStyle.class.getResourceAsStream("bean-42.png")));
            final IconComponent iconComponent = new IconComponent(icon);

            final JPanel borderWrapper = new JPanel();
            borderWrapper.setLayout(new BorderLayout());
            borderWrapper.setBackground(null);
            borderWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            borderWrapper.add(iconComponent, BorderLayout.CENTER);
            panel.add(borderWrapper, BorderLayout.EAST);
        } catch (Exception e) {
            LOG.warn("Unable to load image", e);
        }

        panel.add(logoPanel, BorderLayout.WEST);

        return panel;
    }

    @Override
    public List<? extends Image> getDialogIcons() {
        return ImageResources.INSTANCE.getApplicationImages();
    }
}
