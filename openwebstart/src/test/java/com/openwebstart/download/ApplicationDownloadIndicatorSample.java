package com.openwebstart.download;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.jnlp.DownloadServiceListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.openwebstart.download.ApplicationDownloadIndicator.DOWNLOAD_INDICATOR;

class ApplicationDownloadIndicatorSample {

    public static void main(String[] args) {
        Translator.addBundle("i18n");
        final ApplicationDownloadIndicator indicator = DOWNLOAD_INDICATOR;
        downloadPart("Demo application - Part1", indicator, 20);
        sleep(1_000);
        downloadPart("Demo application - Part2", indicator, 10);

        System.out.println("DONE!");
    }

    private static void downloadPart(String applicationName, ApplicationDownloadIndicator indicator, int resourceCount) {
        final DownloadServiceListener listener = indicator.getListener(applicationName, new URL[0]);
        new SampleApplication(listener, resourceCount).download();
        indicator.disposeListener(listener);
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

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class SampleApplication {

        private final DownloadServiceListener listener;

        private final List<SampleResource> resources;

        public SampleApplication(final DownloadServiceListener listener, int resourceCount) {
            this.listener = listener;

            final Random random = new Random(System.nanoTime());
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
                        final int overallPercentage = resources.stream().mapToInt(SampleResource::getPercentage).sum() / resources.size();
                        listener.progress(resource.url, resource.version, resource.readSoFar, resource.size, overallPercentage);
                        sleep(random.nextInt(50));
                    }
                }
            }
            System.out.println("Download finish");
        }

        public boolean isDownloaded() {
            return resources.stream().allMatch(SampleResource::isDownloaded);
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
            final double fraction = ((double) readSoFar) / size;
            return (int) Math.min(100, (long) (fraction * 100));
        }

        public boolean isDownloaded() {
            return size == readSoFar;
        }
    }
}
