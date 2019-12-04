package com.openwebstart.proxy.mac;

import com.openwebstart.proxy.ProxyProvider;
import com.openwebstart.util.ProcessUtil;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MacProxyProvider implements ProxyProvider {

    public MacProxyProvider() throws IOException, InterruptedException, ExecutionException {
        final Process process = new ProcessBuilder()
                .command("scutil", "--proxy")
                .redirectErrorStream(true)
                .start();

        final Future<String> out = ProcessUtil.getIO(process.getInputStream());
        final int exitValue = process.waitFor();
        if(exitValue != 0) {
            throw new RuntimeException("process ended with error code " + exitValue);
        }
        final String processOut = out.get();

        final MacProxySettings proxySettings = ScutilParser.parse(processOut);

        if(proxySettings.isAutoDiscoveryEnabled()) {
            //TODO: Notify User that unsupported proxy settings are configured!
        }
        if(proxySettings.isExcludeSimpleHostnames()) {
            //TODO: Notify User that unsupported proxy settings are configured!
        }
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return null;
    }
}
