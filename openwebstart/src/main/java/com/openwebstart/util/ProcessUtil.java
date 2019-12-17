package com.openwebstart.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    public static String executeProcessAndReturnOutput(String... args) throws IOException, InterruptedException, ExecutionException {
        final Process process = new ProcessBuilder()
                .command(args)
                .redirectErrorStream(true)
                .start();
        final Future<String> out = ProcessUtil.getIO(process.getInputStream());
        final int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("process ended with error code " + exitValue);
        }
        return out.get();
    }

    public static void logIO(final InputStream src) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final Scanner sc = new Scanner(src);
            while (sc.hasNextLine()) {
                LOG.debug("APP: " + sc.nextLine());
            }
        });
    }

    public static Future<String> getIO(final InputStream src) {
        final CompletableFuture<String> result = new CompletableFuture<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final StringBuilder builder = new StringBuilder();
                final Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    builder.append(sc.nextLine());
                    if (sc.hasNextLine()) {
                        builder.append(System.lineSeparator());
                    }
                }
                result.complete(builder.toString());
            } catch (final Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }
}
