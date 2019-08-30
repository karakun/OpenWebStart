package com.openwebstart.jvm;

import com.openwebstart.func.Result;
import com.openwebstart.func.Success;
import com.openwebstart.http.HttpGetRequest;
import com.openwebstart.http.HttpResponse;
import com.openwebstart.jvm.json.JsonHandler;
import com.openwebstart.jvm.json.RemoteRuntimeList;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import com.openwebstart.jvm.runtimes.Vendor;
import com.openwebstart.jvm.util.RemoteRuntimeManagerCache;
import com.openwebstart.jvm.util.RuntimeVersionComparator;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.openwebstart.jvm.runtimes.Vendor.ANY_VENDOR;

class RemoteRuntimeManager {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteRuntimeManager.class);

    private static final RemoteRuntimeManager INSTANCE = new RemoteRuntimeManager();

    private final AtomicReference<RemoteRuntimeManagerCache> cache = new AtomicReference<>();

    private RemoteRuntimeManager() {
    }

    public Optional<RemoteJavaRuntime> getBestRuntime(final VersionString versionString, final URL specificServerEndpoint) {
        final String vendorName = RuntimeManagerConfig.getVendor();
        return getBestRuntime(versionString, specificServerEndpoint, Vendor.fromString(vendorName), OperationSystem.getLocalSystem());
    }

    public Optional<RemoteJavaRuntime> getBestRuntime(final VersionString versionString, final URL specificServerEndpoint, final Vendor vendor, final OperationSystem operationSystem) {
        Assert.requireNonNull(versionString, "versionString");
        Assert.requireNonNull(vendor, "vendor");
        Assert.requireNonNull(operationSystem, "operationSystem");


        LOG.debug("Trying to find remote Java runtime. Requested version: '{}' Requested vendor: '{}' requested os: '{}'", versionString, vendor, operationSystem);

        final URL endpointForRequest = Optional.ofNullable(specificServerEndpoint)
                .filter(e -> RuntimeManagerConfig.isNonDefaultServerAllowed())
                .orElse(RuntimeManagerConfig.getDefaultRemoteEndpoint());

        LOG.debug("Endpoint to request for Java runtimes: {}", endpointForRequest);

        final Result<RemoteRuntimeList> result = Optional.ofNullable(cache.get())
                .filter(RemoteRuntimeManagerCache::isStillValid)
                .filter(c -> Objects.equals(endpointForRequest, c.getEndpointForRequest()))
                .map(c -> (Result<RemoteRuntimeList>) new Success<>(c.getList()))
                .orElseGet(Result.of(() -> {
                    final HttpGetRequest request = new HttpGetRequest(endpointForRequest);
                    try (final HttpResponse response = request.handle()) {
                        final String jsonContent = IOUtils.readContentAsUtf8String(response.getContentStream());
                        final RemoteRuntimeList receivedList = JsonHandler.getInstance().fromJson(jsonContent, RemoteRuntimeList.class);
                        cache.set(new RemoteRuntimeManagerCache(endpointForRequest, receivedList));
                        return receivedList;
                    }
                }));

        if (result.isSuccessful()) {
            Assert.requireNonNull(vendor, "vendorForRequest");

            LOG.debug("Received {} possible runtime definitions from server", result.getResult().getRuntimes().size());

            return result.getResult().getRuntimes().stream()
                    .filter(r -> r.getOperationSystem() == operationSystem)
                    .filter(r -> Objects.equals(vendor, ANY_VENDOR) || Objects.equals(vendor, r.getVendor()))
                    .filter(r -> versionString.contains(r.getVersion()))
                    .filter(r -> Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(v -> v.contains(r.getVersion())).orElse(true))
                    .max(new RuntimeVersionComparator(versionString));
        } else {
            LOG.error("Error while trying to find a remote version", result.getException());
            return Optional.empty();
        }
    }

    public static RemoteRuntimeManager getInstance() {
        return INSTANCE;
    }
}
