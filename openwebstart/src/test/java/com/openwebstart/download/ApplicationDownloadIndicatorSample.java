package com.openwebstart.download;

import javax.jnlp.DownloadServiceListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ApplicationDownloadIndicatorSample {

    public static void main(String[] args) {
        final String applicationName = "Demo application";
        final URL[] resources = new URL[0];
        final ApplicationDownloadIndicator indicator = new ApplicationDownloadIndicator();
        final DownloadServiceListener listener = indicator.getListener(applicationName, resources);

        final SampleApplication application = new SampleApplication(listener);
        application.download();

        try {
            Thread.sleep(2_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        indicator.disposeListener(listener);

        System.out.println("DONE!");
    }

    private static SampleResource createRandomResource(Random random, int number) {
        try {
            final URL url = new URL("http://www.sample.com/jnlp/jar_" + number + ".jar");
            final String version = random.nextInt(12) + "." + random.nextInt(5) + ".0";
            final long size = random.nextInt(500_000);
            return new SampleResource(url, version, size);
        } catch (MalformedURLException e) {
            throw new RuntimeException("ERROR", e);
        }

    }

    private static class SampleApplication {

        private final DownloadServiceListener listener;

        private final List<SampleResource> resources;

        public SampleApplication(final DownloadServiceListener listener) {
            this.listener = listener;

            final Random random = new Random(System.nanoTime());
            final int resourceCount = random.nextInt(32) + 4;
            this.resources = IntStream.range(0, resourceCount)
                    .mapToObj(i -> createRandomResource(random, i))
                    .collect(Collectors.toList());
        }

        public void download() {
            final Random random = new Random(System.nanoTime());
            System.out.println("Download start");
            while (!isDownloaded()) {
                System.out.println("Download still in progress");

                for (SampleResource resource : resources) {
                    if (!resource.isDownloaded() && random.nextBoolean()) {
                        final int percentage = random.nextInt(4) + 1;
                        resource.increase(percentage);
                        System.out.println(resource.url + " -> " + resource.getPercentage() + "%");
                        final int overallPercentage = resources.stream().mapToInt(r -> r.getPercentage()).sum() / resources.size();
                        listener.progress(resource.url, resource.version, resource.readSoFar, resource.size, overallPercentage);
                        try {
                            Thread.sleep(random.nextInt(50));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.println("Download finish");
        }

        public boolean isDownloaded() {
            return resources.stream().map(r -> r.isDownloaded())
                    .filter(d -> !d)
                    .count() == 0;
        }
    }

    private static class SampleResource {

        private final URL url;

        private final String version;

        private long readSoFar = 0;

        private final long size;

        public SampleResource(final URL url, final String version, final long size) {
            this.url = url;
            this.version = version;
            this.size = size;
        }

        public URL getUrl() {
            return url;
        }

        public String getVersion() {
            return version;
        }

        public synchronized void increase(int percentage) {
            long value = (size / 100) * percentage;
            this.readSoFar = Math.min(size, readSoFar + value);
        }

        public int getPercentage() {
            return (int) Math.min((long) Integer.MAX_VALUE, readSoFar / (size / 100));
        }

        public boolean isDownloaded() {
            return size == readSoFar;
        }
    }
}
