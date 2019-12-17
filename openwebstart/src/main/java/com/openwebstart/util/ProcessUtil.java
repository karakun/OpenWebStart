package com.openwebstart.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    private static final ExecutorService READER_EXECUTOR = Executors.newCachedThreadPool();

    public static ProcessResult runProcess(final ProcessBuilder builder, long timeout, TimeUnit timeUnit) throws Exception {
        Assert.requireNonNull(builder, "builder");
        Assert.requireNonNull(timeUnit, "timeUnit");

        final Process p = builder.start();
        final Future<String> stdFuture = READER_EXECUTOR.submit(() -> IOUtils.readContentAsUtf8String(p.getInputStream()));
        final Future<String> errFuture = READER_EXECUTOR.submit(() -> IOUtils.readContentAsUtf8String(p.getErrorStream()));
        try {
            final int returnCode = waitFor(p, timeout, timeUnit);
            final String standardOut = stdFuture.get(1, TimeUnit.SECONDS);
            final String errorOut = errFuture.get(1, TimeUnit.SECONDS);
            return new ProcessResult(returnCode, standardOut, errorOut);
        } catch (final Exception e) {
            if (!stdFuture.cancel(true)) {
                LOG.warn("thread for standard out for '{}' was not terminated!", builder.command());
            }
            if (!errFuture.cancel(true)) {
                LOG.warn("thread for err out for '{}' was not terminated!", builder.command());
            }
            throw e;
        }
    }

    private static int waitFor(final Process process, long timeout, TimeUnit unit) throws Exception {
        if (process.waitFor(timeout, unit)) {
            return process.exitValue();
        }
        throw new TimeoutException("Process not finished in given time periode");
    }

}
