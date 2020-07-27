package com.openwebstart.ui;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

public class OpenWebStartDialogHeader extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebStartDialogHeader.class);

    private Image backgroundImage;

    public OpenWebStartDialogHeader() {
        try {
            backgroundImage = ImageIO.read(OpenWebStartDialogHeader.class.getResource("dialog-head-back.png"));
            setPreferredSize(new Dimension(800, 64));
            setMaximumSize(getPreferredSize());
            setMinimumSize(new Dimension(1, 1));
            setBackground(new Color(19, 65, 95));
            setLayout(null);

            final JLabel openLabel = new JLabel("Open");
            try {
                openLabel.setFont(Font.createFont(Font.TRUETYPE_FONT, OpenWebStartDialogHeader.class.getResource("FiraSans-Bold.ttf").openStream()));
            } catch (final Exception e) {
                LOG.warn("Unable to load font", e);
            }
            openLabel.setForeground(new Color(222, 219, 0));
            openLabel.setFont(openLabel.getFont().deriveFont(28.0f));
            openLabel.setFont(openLabel.getFont().deriveFont(Font.BOLD));
            add(openLabel);
            openLabel.setLocation(6, 0);
            openLabel.setSize(new Dimension(openLabel.getPreferredSize().width + 4, openLabel.getPreferredSize().height + 4));


            final JLabel webStartLabel = new JLabel("WebStart");
            try {
                webStartLabel.setFont(Font.createFont(Font.TRUETYPE_FONT, OpenWebStartDialogHeader.class.getResource("FiraSans-Regular.ttf").openStream()));
            } catch (final Exception e) {
                LOG.warn("Unable to load font", e);
            }
            webStartLabel.setForeground(Color.WHITE);
            webStartLabel.setFont(webStartLabel.getFont().deriveFont(28.0f));
            webStartLabel.setFont(webStartLabel.getFont().deriveFont(Font.PLAIN));

            add(webStartLabel);
            webStartLabel.setLocation(74, 0);
            webStartLabel.setSize(new Dimension(webStartLabel.getPreferredSize().width + 4, webStartLabel.getPreferredSize().height + 4));
        } catch (IOException e) {
            LOG.error("Can not create dialog header", e);
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, null);
        }
    }
}
