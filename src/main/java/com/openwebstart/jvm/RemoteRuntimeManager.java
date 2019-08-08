package com.openwebstart.jvm;

import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.json.RemoteRuntimeList;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.util.RemoteRuntimeManagerCache;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import dev.rico.client.Client;
import com.openwebstart.rico.functional.CheckedSupplier;
import dev.rico.core.functional.Result;
import dev.rico.core.http.HttpClient;
import dev.rico.core.http.HttpResponse;
import dev.rico.internal.core.functional.Sucess;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteRuntimeManager {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteRuntimeManager.class);

    private static final RemoteRuntimeManager INSTANCE = new RemoteRuntimeManager();

    private final AtomicReference<RemoteRuntimeManagerCache> cache = new AtomicReference<>();

    private RemoteRuntimeManager() {
    }

    public Optional<RemoteJavaRuntime> getBestRuntime(final VersionString versionString, final URI specificServerEndpoint) throws Exception {
        return getBestRuntime(versionString, specificServerEndpoint, RuntimeManagerConstants.VENDOR_ANY);
    }

    public Optional<RemoteJavaRuntime> getBestRuntime(final VersionString versionString, final URI specificServerEndpoint, final String vendor) throws Exception {
        return getBestRuntime(versionString, specificServerEndpoint, vendor, OperationSystem.getLocalSystem());
    }

    public Optional<RemoteJavaRuntime> getBestRuntime(final VersionString versionString, final URI specificServerEndpoint, final String vendor, final OperationSystem operationSystem) throws Exception {
        Assert.requireNonNull(versionString, "versionString");
        Assert.requireNonBlank(vendor, "vendor");
        Assert.requireNonNull(operationSystem, "operationSystem");

        LOG.debug("Trying to find remote Java runtime. Requested version: '" + versionString + "' Requested vendor: '" + vendor + "' requested os: '" + operationSystem + "'");

        final URI endpointForRequest = Optional.ofNullable(specificServerEndpoint)
                .map(e -> RuntimeManagerConfig.getInstance().isSpecificRemoteEndpointsEnabled() ? specificServerEndpoint : RuntimeManagerConfig.getInstance().getDefaultRemoteEndpoint())
                .orElse(RuntimeManagerConfig.getInstance().getDefaultRemoteEndpoint());

        Assert.requireNonNull(endpointForRequest, "endpointForRequest");

        LOG.debug("Endpoint to request for Java runtimes: " + endpointForRequest);

        final Result<RemoteRuntimeList> result = Optional.ofNullable(cache.get())
                .filter(c -> c.isStillValid())
                .filter(c -> Objects.equals(endpointForRequest, c.getEndpointForRequest()))
                .map(c -> (Result<RemoteRuntimeList>) new Sucess(c.getList()))
                .orElseGet(CheckedSupplier.of(() -> {
                    final HttpClient client = Client.getService(HttpClient.class);
                    final HttpResponse<String> response = client.get(endpointForRequest)
                            .withoutContent()
                            .readString()
                            .execute()
                            .get(RuntimeManagerConstants.HTTP_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);

                    final String jsonContent = response.getContent();

                    final RemoteRuntimeList receivedList = JsonHandler.getInstance().fromJson(jsonContent, RemoteRuntimeList.class);
                    cache.set(new RemoteRuntimeManagerCache(endpointForRequest, receivedList));
                    return receivedList;
                }));

        if (result.iSuccessful()) {
            final String vendorForRequest = RuntimeManagerConfig.getInstance().isSpecificVendorEnabled() ? vendor : RuntimeManagerConfig.getInstance().getDefaultVendor();
            Assert.requireNonBlank(vendorForRequest, "vendorForRequest");

            LOG.debug("Received " + result.getResult().getRuntimes().size() + " possible runtime defintions from server");

            return result.getResult().getRuntimes().stream()
                    .filter(r -> Objects.equals(r.getOperationSystem(), operationSystem))
                    .filter(r -> Objects.equals(vendorForRequest, RuntimeManagerConstants.VENDOR_ANY) || Objects.equals(vendorForRequest, r.getVendor()))
                    .filter(r -> versionString.contains(r.getVersion()))
                    .filter(r -> Optional.ofNullable(RuntimeManagerConfig.getInstance().getSupportedVersionRange()).map(v -> v.contains(r.getVersion())).orElse(true))
                    .sorted(new RuntimeVersionComparator(versionString).reversed())
                    .findFirst();
        } else {
            throw new Exception("Error while trying to find a remote version", result.getException());
        }
    }

    public static RemoteRuntimeManager getInstance() {
        return INSTANCE;
    }
}
