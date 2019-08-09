/*
 * Copyright 2019 Karakun AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain net.adoptopenjdk.icedteaweb.client.controlpanel.panels.provider.ControlPanelProvider copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openwebstart.jvm.os;

import static com.openwebstart.jvm.os.Architecture.X64;
import static com.openwebstart.jvm.os.Architecture.X86;

public enum OperationSystem {

    ARM32("Linux ARM 32 Hard Float ABI", "arm-32", Architecture.ARM32), ARM64("Linux ARM 64 Hard Float ABI", "arm-64", Architecture.ARM64), LINUX32("Linux x86", "linux-32", X86), LINUX64("Linux x64", "linux-64", X64),
    MAC64("Mac OS X x64", "mac-64", X64), WIN32("Windows x86", "win-32", X86), WIN64("Windows x64", "win-64", X64);

    //TODO: Should be in ITW
    private final static String OS_SYSTEM_PROPERTY = "os.name";

    //TODO: Should be in ITW
    private final static String OS_ARCH_SYSTEM_PROPERTY = "sun.arch.data.model";

    private final static String WIN = "win";

    private final static String LINUX = "linux";

    private final static String MAC = "mac";

    private final static String ARCH_64 = "64";

    private final String name;

    private final String shortName;

    private final Architecture architecture;

    OperationSystem(final String name, final String shortName, final Architecture architecture) {
        this.name = name;
        this.shortName = shortName;
        this.architecture = architecture;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public String getArchitectureName() {
        return architecture.getName();
    }

    public static OperationSystem getLocalSystem() {
        final String osName = System.getProperty(OS_SYSTEM_PROPERTY).toLowerCase();
        final String arch = System.getProperty(OS_ARCH_SYSTEM_PROPERTY).toLowerCase();
        if (osName.contains(WIN)) {
            if (arch.contains(ARCH_64)) {
                return OperationSystem.WIN64;
            } else {
                return OperationSystem.WIN32;
            }
        }
        if (osName.contains(MAC)) {
            return OperationSystem.MAC64;
        }
        if (osName.contains(LINUX)) {
            if (arch.contains(ARCH_64)) {
                return OperationSystem.LINUX64;
            } else {
                return OperationSystem.LINUX32;
            }
        }
        throw new IllegalStateException("Can not specify OS");
    }
}
