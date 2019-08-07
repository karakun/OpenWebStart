package com.openwebstart.maven;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class OwsManifestResourceTransformer implements ResourceTransformer {

    private static final String MANIFEST_MF = "META-INF/MANIFEST.MF";

    private Map<String, Object> manifestEntries; // set by maven via reflection

    private boolean manifestDiscovered;
    private Manifest manifest;

    public boolean canTransformResource(String resource) {
        return MANIFEST_MF.equalsIgnoreCase(resource);
    }

    public void processResource(String resource, InputStream is, List<Relocator> relocators) throws IOException {
        if (!this.manifestDiscovered) {
            this.manifest = new Manifest(is);

            final Attributes mainAttributes = manifest.getMainAttributes();
            this.manifestDiscovered = mainAttributes.containsKey(new Name("Scm-Commit"));
        }
    }

    public boolean hasTransformedResource() {
        return true;
    }

    public void modifyOutputStream(JarOutputStream jos) throws IOException {
        if (!manifestDiscovered) {
            manifest = new Manifest();
        }

        final Attributes attributes = manifest.getMainAttributes();

        if (manifestEntries != null) {
            for (Entry<String, Object> entry : manifestEntries.entrySet()) {
                attributes.put(new Name(entry.getKey()), entry.getValue());
            }
        }

        jos.putNextEntry(new JarEntry(MANIFEST_MF));
        this.manifest.write(jos);
    }
}
