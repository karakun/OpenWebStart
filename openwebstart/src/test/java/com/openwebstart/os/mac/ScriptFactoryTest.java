package com.openwebstart.os.mac;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static com.openwebstart.os.mac.ScriptFactory.createSimpleStartScriptForMac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptFactoryTest {
    @Test
    void testCreateStartScriptForMac_for_http_location() throws Exception {
        final JNLPFileFactory f = new JNLPFileFactory();
        final URL jnlpUrl = this.getClass().getClassLoader()
                .getResource("com/openwebstart/jnlp/with-http-location.jnlp");
        assertNotNull(jnlpUrl);

        final JNLPFile jnlp = f.create(jnlpUrl);
        assertNotNull(jnlp);
        assertEquals(jnlp.getSourceLocation().toString(), "http://localhost/jnlp.jnlp");

        final String script = createSimpleStartScriptForMac(jnlp);
        assertTrue(script.contains("open \"jnlp://localhost/jnlp.jnlp\""));
    }

    @Test
    void testCreateStartScriptForMac_for_https_location() throws Exception {
        final JNLPFileFactory f = new JNLPFileFactory();
        final URL jnlpUrl = this.getClass().getClassLoader()
                .getResource("com/openwebstart/jnlp/with-https-location.jnlp");
        assertNotNull(jnlpUrl);

        final JNLPFile jnlp = f.create(jnlpUrl);
        assertNotNull(jnlp);
        assertEquals(jnlp.getSourceLocation().toString(), "https://localhost/jnlp.jnlp");

        final String script = createSimpleStartScriptForMac(jnlp);
        assertTrue(script.contains("open \"jnlps://localhost/jnlp.jnlp\""));
    }

    @Test
    void testCreateStartScriptForMac_for_file_location() throws Exception {
        final JNLPFileFactory f = new JNLPFileFactory();
        final URL jnlpUrl = this.getClass().getClassLoader()
                .getResource("com/openwebstart/jnlp/with-file-location.jnlp");
        assertNotNull(jnlpUrl);

        final JNLPFile jnlp = f.create(jnlpUrl);
        assertNotNull(jnlp);
        assertEquals(jnlp.getSourceLocation().toString(), "file:/path/jnlp.jnlp");

        final String script = createSimpleStartScriptForMac(jnlp);
        assertTrue(script.contains("open \"file:/path/jnlp.jnlp\""));
    }
}
