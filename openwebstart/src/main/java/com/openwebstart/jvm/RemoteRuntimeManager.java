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
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;

import java.net.URL;
import java.util.Collections;
import java.util.List;
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

    Optional<RemoteJavaRuntime> getBestRuntime(final VersionString versionString, final URL specificServerEndpoint, final Vendor vendor, final OperationSystem operationSystem) {
        Assert.requireNonNull(versionString, "versionString");
        Assert.requireNonNull(vendor, "vendor");
        Assert.requireNonNull(operationSystem, "operationSystem");

        LOG.debug("Trying to find remote Java runtime. Requested version: '{}' Requested vendor: '{}' requested os: '{}'", versionString, vendor, operationSystem);

        // jvm server specified in jnlp is not null and is allowed
        // TODO : What to do if not found in Whitelist
        // TODO : confirm that we only check the URL in JNLP against whitelist and not the URL specified in settings
        if (RuntimeManagerConfig.isNonDefaultServerAllowed() && specificServerEndpoint != null) {
            if (!UrlWhiteListUtils.isUrlInWhitelist(specificServerEndpoint, RuntimeManagerConfig.getJvmServerWhitelist())) {
                LOG.warn("JVM Server URL {} not in JVM Server Whitelist", specificServerEndpoint);
                return Optional.empty();
            }
            LOG.debug("JVM Server URL {} found in JVM Server Whitelist", specificServerEndpoint);
        }
        final URL endpointForRequest = getEndpointForRequest(specificServerEndpoint);
        final List<RemoteJavaRuntime> remoteRuntimes = loadListOfRemoteRuntimes(endpointForRequest);
        return selectBestRuntime(remoteRuntimes, versionString, vendor, operationSystem);
    }

    private URL getEndpointForRequest(URL specificServerEndpoint) {
        final URL endpointForRequest = Optional.ofNullable(specificServerEndpoint)
                .filter(e -> RuntimeManagerConfig.isNonDefaultServerAllowed())
                .orElse(RuntimeManagerConfig.getDefaultRemoteEndpoint());

        LOG.debug("Endpoint to request for Java runtimes: {}", endpointForRequest);
        return endpointForRequest;
    }

    List<RemoteJavaRuntime> loadListOfRemoteRuntimes(URL endpointForRequest) {
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
            LOG.debug("Received {} possible runtime definitions from server", result.getResult().getRuntimes().size());
            return result.getResult().getRuntimes();
        } else {
            LOG.error("Error while trying to find a remote version", result.getException());
            return Collections.emptyList();
        }

    }

    private Optional<RemoteJavaRuntime> selectBestRuntime(List<RemoteJavaRuntime> remoteRuntimes, VersionString versionString, Vendor vendor, OperationSystem operationSystem) {
        final LocalRuntimeManager localRuntimeManager = LocalRuntimeManager.getInstance();
        return remoteRuntimes.stream()
                .filter(r -> r.getOperationSystem() == operationSystem)
                .filter(r -> Objects.equals(vendor, ANY_VENDOR) || Objects.equals(vendor, r.getVendor()))
                .filter(r -> versionString.contains(r.getVersion()))
                .filter(r -> Optional.ofNullable(RuntimeManagerConfig.getSupportedVersionRange()).map(v -> v.contains(r.getVersion())).orElse(true))
                .filter(r -> !localRuntimeManager.hasManagedRuntime(r.getVersion(), r.getVendor()))
                .max(new RuntimeVersionComparator(versionString));
    }

    public static RemoteRuntimeManager getInstance() {
        return INSTANCE;
    }
}
