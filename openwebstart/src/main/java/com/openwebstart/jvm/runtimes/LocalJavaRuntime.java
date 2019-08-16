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
package com.openwebstart.jvm.runtimes;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.Assert;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;

public class LocalJavaRuntime extends JavaRuntime {

    public static LocalJavaRuntime createManaged(final RemoteJavaRuntime remoteJavaRuntime, final Path javaHome) {
        return new LocalJavaRuntime(remoteJavaRuntime, javaHome, LocalDateTime.now(), true, true);
    }

    public static LocalJavaRuntime createPreInstalled(final String version, final OperationSystem operationSystem, final String vendor, final Path javaHome) {
        return new LocalJavaRuntime(version, operationSystem, vendor, javaHome, LocalDateTime.now(), false, false);
    }

    private final Path javaHome;

    private final boolean active;

    private final LocalDateTime lastUsage;

    /**
     * false if runtime is installed on system and not by JVM manager
     */
    private final boolean managed;

    private LocalJavaRuntime(final JavaRuntime javaRuntime, final Path javaHome, final LocalDateTime lastUsage, boolean active, boolean managed) {
        super(javaRuntime);
        this.javaHome = Assert.requireNonNull(javaHome, "javaHome");
        this.lastUsage = Assert.requireNonNull(lastUsage, "lastUsage");
        this.active = active;
        this.managed = managed;
    }

    public LocalJavaRuntime(final String version, final OperationSystem operationSystem, final String vendor, final Path javaHome, final LocalDateTime lastUsage, boolean active, boolean managed) {
        super(version, operationSystem, vendor);
        this.javaHome = Assert.requireNonNull(javaHome, "javaHome");
        this.lastUsage = Assert.requireNonNull(lastUsage, "lastUsage");
        this.active = active;
        this.managed = managed;
    }

    public Path getJavaHome() {
        return javaHome;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LocalJavaRuntime runtime = (LocalJavaRuntime) o;
        return Objects.equals(javaHome, runtime.javaHome) &&
                Objects.equals(getVersion(), runtime.getVersion()) &&
                getOperationSystem() == runtime.getOperationSystem();
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaHome, getVersion(), getOperationSystem());
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastUsage() {
        return lastUsage;
    }

    public boolean isManaged() {
        return managed;
    }

    public LocalJavaRuntime getDeactivatedCopy() {
        return new LocalJavaRuntime(this, getJavaHome(), getLastUsage(), false, isManaged());
    }

    public LocalJavaRuntime getActivatedCopy() {
        return new LocalJavaRuntime(this, getJavaHome(), getLastUsage(), true, isManaged());
    }
}
