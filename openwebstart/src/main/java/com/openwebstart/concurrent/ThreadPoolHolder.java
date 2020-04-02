package com.openwebstart.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ...
 */
public class ThreadPoolHolder {

    private static final ThreadGroup OWS_THREAD_GROUP = new ThreadGroup("OWS parent");

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new OwsThreadFactory(OWS_THREAD_GROUP, true, "daemon-");
    private static final ThreadFactory NON_DAEMON_THREAD_FACTORY = new OwsThreadFactory(OWS_THREAD_GROUP, false, "");

    private static final ExecutorService DAEMON_THREAD_POOL = Executors.newCachedThreadPool(DAEMON_THREAD_FACTORY);
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(NON_DAEMON_THREAD_FACTORY);


    public static ExecutorService getDaemonExecutorService() {
        return DAEMON_THREAD_POOL;
    }

    public static ExecutorService getNonDaemonExecutorService() {
        return THREAD_POOL;
    }

    private static class OwsThreadFactory implements ThreadFactory {
        private final boolean isDaemon;
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        OwsThreadFactory(ThreadGroup parentThreadGroup, final boolean isDaemon, final String name) {
            this.isDaemon = isDaemon;
            group = new ThreadGroup(parentThreadGroup, "OWS " + name + "threads");
            namePrefix = "ows-" + name + "pool-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon() != isDaemon) {
                t.setDaemon(isDaemon);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
