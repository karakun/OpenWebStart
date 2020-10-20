package com.openwebstart.os.mac;


import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.os.mac.icns.IcnsFactorySample;

import java.util.Objects;

public class AppFactorySample {

    private final static String SCRIPT_START = "#!/bin/sh";


    public static void main(String[] args) throws Exception {
        if (!Objects.equals(OperationSystem.getLocalSystem(), OperationSystem.MAC64)) {
            throw new RuntimeException("Sample can only be executed on MacOS");
        }

        final String script = SCRIPT_START + System.lineSeparator() + "open -a Calculator";

        new AppFactory().createApp("MyFirstApp", script, IcnsFactorySample.class.getResource("icon.png").getFile());
    }
}
