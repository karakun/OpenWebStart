package com.openwebstart.jvm.localfinder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ...
 */
class RuntimeFinderTest {

    @Test
    void shouldNotThrowAnException() {
        assertNotNull(RuntimeFinder.find());
    }
}
