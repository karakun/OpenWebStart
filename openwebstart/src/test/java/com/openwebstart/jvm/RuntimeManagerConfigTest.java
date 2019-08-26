package com.openwebstart.jvm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RuntimeManagerConfigTest {

    @Test
    void testChangeOfNonDefaultServerAllowedSetting() {
        final boolean initialValue = RuntimeManagerConfig.isNonDefaultServerAllowed();
        RuntimeManagerConfig.setNonDefaultServerAllowed(!initialValue);
        final boolean newValue = RuntimeManagerConfig.isNonDefaultServerAllowed();
        Assertions.assertNotEquals(newValue, initialValue);
    }
}