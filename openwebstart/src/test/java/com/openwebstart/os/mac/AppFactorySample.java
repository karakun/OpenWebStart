package com.openwebstart.os.mac;


import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.os.mac.icns.IcnsFactorySample;

import java.util.Objects;

public class AppFactorySample {

    public static void main(String[] args) throws Exception {
        if(!Objects.equals(OperationSystem.getLocalSystem(),OperationSystem.MAC64)) {
            throw new RuntimeException("Sample can only be executed on MacOS");
        }
        new AppFactory().createApp("MyFirstApp", IcnsFactorySample.class.getResource("icon.png").getFile());
    }
}
