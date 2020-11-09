package com.openwebstart.jvm.localfinder;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ...
 */
class JdkFinderTest {

    @Test
    void shouldNotThrowAnException() {
        assertNotNull(JdkFinder.findLocalRuntimes(new DeploymentConfiguration()));
    }
}
