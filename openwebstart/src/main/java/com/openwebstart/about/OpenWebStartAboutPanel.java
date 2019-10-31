package com.openwebstart.about;

import com.openwebstart.install4j.Install4JUtils;
import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class OpenWebStartAboutPanel extends JPanel {

    public OpenWebStartAboutPanel(final DeploymentConfiguration deploymentConfiguration) {
        Assert.requireNonNull(deploymentConfiguration, "deploymentConfiguration");

        setLayout(new BorderLayout());

        JLabel label = new JLabel("Open Webstart" + Install4JUtils.applicationVersion().map(v -> " " + v).orElse(" X"));

        add(label, BorderLayout.CENTER);
    }
}
