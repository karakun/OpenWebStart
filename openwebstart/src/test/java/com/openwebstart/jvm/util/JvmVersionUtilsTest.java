package com.openwebstart.jvm.util;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link JvmVersionUtils}.
 */
class JvmVersionUtilsTest {

    @Test
    void shouldSanitizeTheVersionForJvmSystemProperties() {
        assertSanitized("8.35", "1.8.35");
        assertSanitized("4.14", "1.4.14");

        assertUnchangedBySanitation("1.4.162");
        assertUnchangedBySanitation("1.6.552");
        assertUnchangedBySanitation("1.8.35");
        assertUnchangedBySanitation("9.288");
        assertUnchangedBySanitation("11.0.288");

        assertInvalidJvmVersion("1.9.8");
        assertInvalidJvmVersion("1.11.66");
    }

    private void assertUnchangedBySanitation(String input) {
        assertSanitized(input, input);
    }

    private void assertSanitized(String input, String expectedOutput) {
        final VersionId result = JvmVersionUtils.fromString(input);
        assertEquals(expectedOutput, result.toString());
    }

    private void assertInvalidJvmVersion(String input) {
        try {
            JvmVersionUtils.fromString(input);
            fail("version " + input + " should have been detected as an invalid JVM version number");
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldPostfixShortVersionWithoutModifier() {
        assertFromJnlpPostfixed("1", "1*");
        assertFromJnlpPostfixed("1.7", "1.7*");
        assertFromJnlpPostfixed("1.8", "1.8*");
        assertFromJnlpPostfixed("1.9", "1.9*");
        assertFromJnlpPostfixed("11.0", "11.0*");
        assertFromJnlpPostfixed("11", "11*");

        assertFromJnlpUnchanged("1.8.122");
        assertFromJnlpUnchanged("11.0.2");
        assertFromJnlpUnchanged("1.8*");
        assertFromJnlpUnchanged("1.8+");
    }

    private void assertFromJnlpUnchanged(String input) {
        assertFromJnlpPostfixed(input, input);
    }

    private void assertFromJnlpPostfixed(String input, String expectedOutput) {
        final VersionString result = JvmVersionUtils.fromJnlp(VersionString.fromString(input));
        assertEquals(expectedOutput, result.toString());
    }
}
