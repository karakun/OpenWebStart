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

import net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants;

import java.util.Optional;

import static com.openwebstart.jvm.os.Architecture.AARCH64;
import static com.openwebstart.jvm.os.Architecture.X64;
import static com.openwebstart.jvm.os.Architecture.X86;

public enum OperationSystem {

    ARM32("Linux ARM 32 Hard Float ABI", "arm-32", Architecture.ARM32),
    ARM64("Linux ARM 64 Hard Float ABI", "arm-64", Architecture.ARM64, ARM32),
    LINUX32("Linux x86", "linux-32", X86),
    LINUX64("Linux x64", "linux-64", X64, LINUX32),
    MAC64("Mac OS X x64", "mac-64", X64),
    MACARM64("Mac OS X aarch64", "mac-arm-64", AARCH64),
    WIN32("Windows x86", "win-32", X86),
    WIN64("Windows x64", "win-64", X64, WIN32),
    ;

    /**
     * System property that contains "32" or "64" to indicate a 32-bit or 64-bit JVM.
     */
    public static final String OS_BITNESS = "sun.arch.data.model";

    private static final String WIN = "win";

    private static final String LINUX = "linux";

    private static final String MAC = "mac";

    private static final String ARCH_64 = "64";

    private final String name;

    private final String shortName;

    private final Architecture architecture;

    private final OperationSystem variant32bit;

    OperationSystem(final String name, final String shortName, final Architecture architecture) {
        this(name, shortName, architecture, null);
    }

    OperationSystem(final String name, final String shortName, final Architecture architecture, final OperationSystem variant32bit) {
        this.name = name;
        this.shortName = shortName;
        this.architecture = architecture;
        this.variant32bit = variant32bit == null ? this : variant32bit;
    }

    public boolean isMac() {
        return this == MAC64 || this == MACARM64;
    }

    public boolean isWindows() {
        return this == WIN64 || this == WIN32;
    }

    public boolean isLinux() {
        return this == LINUX64 || this == LINUX32;
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

    public OperationSystem getVariant32bit() {
        return variant32bit;
    }

    public static OperationSystem getLocalSystem() {
        final String osName = System.getProperty(JavaSystemPropertiesConstants.OS_NAME).toLowerCase();
        final String arch = System.getProperty(JavaSystemPropertiesConstants.OS_ARCH).toLowerCase();
        final String bitness = System.getProperty(OS_BITNESS).toLowerCase();
        return getOperationSystem(osName, arch, bitness).orElseThrow(() -> new IllegalStateException("Cannot specify OS"));
    }

    public static Optional<OperationSystem> getOperationSystem(String osName, String arch, String bitness) {
        if (osName.toLowerCase().contains(WIN)) {
            if (bitness.contains(ARCH_64)) {
                return Optional.of(WIN64);
            } else {
                return Optional.of(WIN32);
            }
        }
        if (osName.toLowerCase().contains(MAC)) {
            if (arch.toLowerCase().contains(AARCH64.getName())) {
                return Optional.of(MACARM64);
            }
            return Optional.of(MAC64);
        }
        if (osName.toLowerCase().contains(LINUX)) {
            if (bitness.contains(ARCH_64)) {
                return Optional.of(LINUX64);
            } else {
                return Optional.of(LINUX32);
            }
        }
        return Optional.empty();
    }
}
