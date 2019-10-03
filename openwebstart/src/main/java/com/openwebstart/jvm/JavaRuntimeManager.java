package com.openwebstart.jvm;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.launcher.JavaRuntimeProvider;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Main interface to the JVM Manager functionalities.
 */
public class JavaRuntimeManager {

    public static JavaRuntimeProvider getJavaRuntimeProvider(
            final BiConsumer<RemoteJavaRuntime, DownloadInputStream> downloadHandler,
            final Predicate<RemoteJavaRuntime> askForUpdateFunction) {

        LocalRuntimeManager.getInstance().loadRuntimes();
        return new JavaRuntimeSelector(downloadHandler, askForUpdateFunction);
    }

    public static String[] getAllVendors(URL serverEndpoint) {

        final Set<String> vendors = new HashSet<>();

        LocalRuntimeManager.getInstance().loadRuntimes();
        List<LocalJavaRuntime> localList = LocalRuntimeManager.getInstance().getAll();
        List<RemoteJavaRuntime> remoteList = RemoteRuntimeManager.getInstance().loadListOfRemoteRuntimes(serverEndpoint);

        localList.forEach(rt -> vendors.add(rt.getVendor().getName()));
        remoteList.forEach(rt -> vendors.add(rt.getVendor().getName()));

        final List<String> vendorList = new ArrayList<>(vendors);
        vendorList.sort(String::compareTo);
        vendorList.add(0, Vendor.ANY_VENDOR.getName());
        return vendorList.toArray(new String[0]);
    }

    public static void reloadLocalRuntimes() {
        LocalRuntimeManager.getInstance().loadRuntimes();
    }
}
