package com.openwebstart.os.mac;

import java.net.URL;

import org.junit.jupiter.api.Test;

import com.openwebstart.os.ScriptFactory;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;

class ScriptFactoryTest 
{
	@Test
	void testCreateStartScriptForMac_for_http_location()  
		throws Exception
	{
		final JNLPFileFactory f = new JNLPFileFactory();
		final URL jnlpUrl = this.getClass().getClassLoader().getResource("com/openwebstart/jnlp/with-http-location.jnlp");
		assert jnlpUrl!=null;
		
		final JNLPFile jnlp = f.create(jnlpUrl);
		assert jnlp!=null;
		assert "http://localhost/jnlp.jnlp".equals(jnlp.getSourceLocation().toString());
		
		final String script = ScriptFactory.createSimpleStartScriptForMac(jnlp);
		assert script.contains("open \"jnlp://localhost/jnlp.jnlp\"");
	}

	@Test
	void testCreateStartScriptForMac_for_https_location()  
		throws Exception
	{
		final JNLPFileFactory f = new JNLPFileFactory();
		final URL jnlpUrl = this.getClass().getClassLoader().getResource("com/openwebstart/jnlp/with-https-location.jnlp");
		assert jnlpUrl!=null;
		
		final JNLPFile jnlp = f.create(jnlpUrl);
		assert jnlp!=null;
		assert "https://localhost/jnlp.jnlp".equals(jnlp.getSourceLocation().toString());
		
		final String script = ScriptFactory.createSimpleStartScriptForMac(jnlp);
		assert script.contains("open \"jnlps://localhost/jnlp.jnlp\"");
	}

	@Test
	void testCreateStartScriptForMac_for_file_location()  
		throws Exception
	{
		final JNLPFileFactory f = new JNLPFileFactory();
		final URL jnlpUrl = this.getClass().getClassLoader().getResource("com/openwebstart/jnlp/with-file-location.jnlp");
		assert jnlpUrl!=null;
		
		final JNLPFile jnlp = f.create(jnlpUrl);
		assert jnlp!=null;
		assert "file:/path/jnlp.jnlp".equals(jnlp.getSourceLocation().toString());
		
		final String script = ScriptFactory.createSimpleStartScriptForMac(jnlp);
		assert script.contains("open \"file:/path/jnlp.jnlp\"");
	}
}
