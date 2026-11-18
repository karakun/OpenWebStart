package com.openwebstart.jvm;

import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Public entry point to search the runtimes offered by the JVM server and to install one of them.
 */
public final class RemoteRuntimeSearch {

    private RemoteRuntimeSearch() {
    }

    /**
     * Searches the runtimes of the configured JVM server that fit the local system and are not installed yet.
     *
     * @param query text like "1.8", "8", "17" or a vendor name. A blank query returns everything.
     */
    public static List<RemoteJavaRuntime> search(final String query) {
        final OperationSystem os = OperationSystem.getLocalSystem();
        final String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        final LocalRuntimeManager local = LocalRuntimeManager.getInstance();

        return RemoteRuntimeManager.getInstance().loadListOfRemoteRuntimes(RuntimeManagerConfig.getDefaultRemoteEndpoint()).stream()
                .filter(r -> r.getOperationSystem() == os)
                .filter(r -> !local.hasManagedRuntime(r.getVersion(), r.getVendor(), r.getOperationSystem()))
                .filter(r -> matches(r, q))
                .sorted((a, b) -> b.getVersion().compareTo(a.getVersion()))
                .collect(Collectors.toList());
    }

    private static boolean matches(final RemoteJavaRuntime r, final String q) {
        if (q.isEmpty()) {
            return true;
        }
        final String version = r.getVersion().toString().toLowerCase(Locale.ROOT);
        final String vendor = r.getVendor().toString().toLowerCase(Locale.ROOT);
        return version.startsWith(q) || version.startsWith("1." + q) || version.contains(q) || vendor.contains(q);
    }

    /**
     * Downloads and installs the given runtime in the JVM cache and registers it in the JVM Manager.
     */
    public static LocalJavaRuntime install(final RemoteJavaRuntime runtime) throws IOException {
        return LocalRuntimeManager.getInstance().install(runtime, null);
    }
}
