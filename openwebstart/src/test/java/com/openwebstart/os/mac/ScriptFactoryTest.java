package com.openwebstart.os.mac;

import com.openwebstart.os.ScriptFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptFactoryTest {
    @Test
    void testCreateStartScriptForMac_for_http_location() throws Exception {
        final JNLPFileFactory f = new JNLPFileFactory();
        final URL jnlpUrl = this.getClass().getClassLoader()
                .getResource("com/openwebstart/jnlp/with-http-location.jnlp");
        assertTrue(jnlpUrl != null);

        final JNLPFile jnlp = f.create(jnlpUrl);
        assertTrue(jnlp != null);
        assertTrue("http://localhost/jnlp.jnlp".equals(jnlp.getSourceLocation().toString()));

        final String script = ScriptFactory.createSimpleStartScriptForMac(jnlp);
        assertTrue(script.contains("open \"jnlp://localhost/jnlp.jnlp\""));
    }

    @Test
    void testCreateStartScriptForMac_for_https_location() throws Exception {
        final JNLPFileFactory f = new JNLPFileFactory();
        final URL jnlpUrl = this.getClass().getClassLoader()
                .getResource("com/openwebstart/jnlp/with-https-location.jnlp");
        assertTrue(jnlpUrl != null);

        final JNLPFile jnlp = f.create(jnlpUrl);
        assertTrue(jnlp != null);
        assertTrue("https://localhost/jnlp.jnlp".equals(jnlp.getSourceLocation().toString()));

        final String script = ScriptFactory.createSimpleStartScriptForMac(jnlp);
        assertTrue(script.contains("open \"jnlps://localhost/jnlp.jnlp\""));
    }

    @Test
    void testCreateStartScriptForMac_for_file_location() throws Exception {
        final JNLPFileFactory f = new JNLPFileFactory();
        final URL jnlpUrl = this.getClass().getClassLoader()
                .getResource("com/openwebstart/jnlp/with-file-location.jnlp");
        assertTrue(jnlpUrl != null);

        final JNLPFile jnlp = f.create(jnlpUrl);
        assertTrue(jnlp != null);
        assertTrue("file:/path/jnlp.jnlp".equals(jnlp.getSourceLocation().toString()));

        final String script = ScriptFactory.createSimpleStartScriptForMac(jnlp);
        assertTrue(script.contains("open \"file:/path/jnlp.jnlp\""));
    }
}
