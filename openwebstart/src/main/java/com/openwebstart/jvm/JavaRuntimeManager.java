package com.openwebstart.jvm;

import com.openwebstart.http.DownloadInputStream;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.launcher.JavaRuntimeProvider;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
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
            final Predicate<RemoteJavaRuntime> askForUpdateFunction,
            final DeploymentConfiguration configuration) {

        LocalRuntimeManager.getInstance().loadRuntimes(configuration);
        return new JavaRuntimeSelector(downloadHandler, askForUpdateFunction);
    }

    public static List<Vendor> getAllVendors(URL serverEndpoint, DeploymentConfiguration configuration) {

        final Set<Vendor> vendors = new HashSet<>();

        LocalRuntimeManager.getInstance().loadRuntimes(configuration);
        List<LocalJavaRuntime> localList = LocalRuntimeManager.getInstance().getAll();
        List<RemoteJavaRuntime> remoteList = RemoteRuntimeManager.getInstance().loadListOfRemoteRuntimes(serverEndpoint);

        localList.forEach(rt -> vendors.add(rt.getVendor()));
        remoteList.forEach(rt -> vendors.add(rt.getVendor()));

        final List<Vendor> vendorList = new ArrayList<>(vendors);
        vendorList.sort(Comparator.comparing(Vendor::getName));
        vendorList.add(0, Vendor.ANY_VENDOR);
        return vendorList;
    }

    public static void reloadLocalRuntimes(DeploymentConfiguration configuration) {
        LocalRuntimeManager.getInstance().loadRuntimes(configuration);
    }
}
