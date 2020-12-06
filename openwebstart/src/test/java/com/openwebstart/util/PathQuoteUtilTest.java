package com.openwebstart.util;

import com.install4j.runtime.launcher.util.LauncherUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PathQuoteUtilTest {

    @Test
    public void testUnquotedClasspathWithoutSpaces() {
        //given
        final String classpathWithoutSpaces = "c:\\Applications\\OpenWebStart\\javaws.jar";
        final String expected = "c:\\Applications\\OpenWebStart\\javaws.jar";

        //when
        final String transformed = PathQuoteUtil.quoteIfRequired(classpathWithoutSpaces);

        //than
        Assertions.assertEquals(expected, transformed);
    }

    @Test
    public void testUnqotedClasspathWithSpaces() {
        //given
        final String classpathWithSpaces = "c:\\Program Files\\OpenWebStart\\javaws.jar";
        final String expectedWindows = "\"c:\\Program Files\\OpenWebStart\\javaws.jar\"";
        final String expectedUnix = "c:\\Program Files\\OpenWebStart\\javaws.jar";

        //when
        final String transformed = PathQuoteUtil.quoteIfRequired(classpathWithSpaces);

        //than
        if (LauncherUtil.isWindows()) {
            Assertions.assertEquals(expectedWindows, transformed);
        } else {
            Assertions.assertEquals(expectedUnix, transformed);
        }
    }

    @Test
    public void testAlreadyQuotedClasspathWithSpaces() {
        //given
        final String quotedClasspathWithSpaces = "\"c:\\Program Files\\OpenWebStart\\javaws.jar\"";
        final String expected = "\"c:\\Program Files\\OpenWebStart\\javaws.jar\"";

        //when
        final String transformed = PathQuoteUtil.quoteIfRequired(quotedClasspathWithSpaces);

        //than
        Assertions.assertEquals(expected, transformed);
    }

    @Test
    public void testNullClasspath() {
        Assertions.assertNull(PathQuoteUtil.quoteIfRequired(null));
    }
}
