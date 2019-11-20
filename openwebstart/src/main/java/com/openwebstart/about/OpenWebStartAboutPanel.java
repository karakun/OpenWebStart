package com.openwebstart.about;

import com.openwebstart.install4j.Install4JUtils;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class OpenWebStartAboutPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebStartAboutPanel.class);


    public OpenWebStartAboutPanel(final DeploymentConfiguration deploymentConfiguration) {
        Assert.requireNonNull(deploymentConfiguration, "deploymentConfiguration");

        setLayout(new BorderLayout());

        JEditorPane editorPane = new JEditorPane();
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        //Do not scroll automatically to bottom
        DefaultCaret caret = (DefaultCaret) editorPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        editorPane.setEditable(false);
        try {
            editorPane.setContentType("text/html");
            editorPane.setText(getHtmlContent());
        } catch (final Exception e) {
            LOG.error("Can not load about document", e);
            editorPane.setText("Can not load about document");
        }
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    LOG.error("Can not open link", ex);
                }
            }
        });

        editorPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 3, 0),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1)));
    }

    private String getHtmlContent() throws IOException {
        try(final InputStream inputStream = OpenWebStartAboutPanel.class.getResourceAsStream("about.template.html")) {
            return IOUtils.readContentAsUtf8String(inputStream)
                    .replaceAll(Pattern.quote("${version}"), Install4JUtils.applicationVersion().orElse(""));
        }
    }
}
