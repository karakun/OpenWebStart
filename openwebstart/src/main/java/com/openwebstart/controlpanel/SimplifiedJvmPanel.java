package com.openwebstart.controlpanel;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.client.controlpanel.DocumentAdapter;
import net.adoptopenjdk.icedteaweb.client.controlpanel.NamedBorderPanel;
import net.adoptopenjdk.icedteaweb.client.controlpanel.panels.JVMPanel;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class SimplifiedJvmPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(JVMPanel.class);

    public SimplifiedJvmPanel(DeploymentConfiguration config) {
        super(new GridBagLayout());

        final JLabel description = new JLabel("<html>" + Translator.R("CPJVMPluginArguments") + "<hr /></html>");
        final JTextField testFieldArguments = new JTextField(25);

        testFieldArguments.getDocument().addDocumentListener(new DocumentAdapter(config, ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS));
        testFieldArguments.setText(config.getProperty(ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS));


        // Filler to pack the bottom of the panel.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 4, 4);

        this.add(description, c);
        c.gridy++;
        this.add(testFieldArguments, c);
        c.gridy++;

        // This is to keep it from expanding vertically if resized.
        Component filler = Box.createRigidArea(new Dimension(1, 1));
        c.gridy++;
        c.weighty++;
        this.add(filler, c);
    }
}
