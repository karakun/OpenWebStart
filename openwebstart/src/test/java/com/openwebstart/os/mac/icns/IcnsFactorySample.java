package com.openwebstart.os.mac.icns;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Objects;

public class IcnsFactorySample {

    public static void main(String[] args) throws Exception {
        if(!Objects.equals(OperationSystem.getLocalSystem(),OperationSystem.MAC64)) {
            throw new RuntimeException("Sample can only be executed on MacOS");
        }
        final File input = new File(IcnsFactorySample.class.getResource("icon.png").getFile());
        final File iconFile = new IcnsFactory().createIconSet(Collections.singletonList(input));
        final File output = new File(System.getProperty("user.home") + "/Desktop/icons.icns");
        try(final InputStream inputStream = new FileInputStream(iconFile); final OutputStream outputStream = new FileOutputStream(output)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }
}
