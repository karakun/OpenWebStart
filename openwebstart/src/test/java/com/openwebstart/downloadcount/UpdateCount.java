package com.openwebstart.downloadcount;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class UpdateCount {

    private static final String PATH = "/Users/hendrikebbers/Desktop/requests.json";

    private static final String[] MONTH = {"2019-09-01", "2019-10-01", "2019-11-01",
            "2019-12-01", "2020-01-01", "2020-02-01",
            "2020-03-01", "2020-04-01"};


    public static void main(String[] args) throws Exception {
        try (final FileReader fileReader = new FileReader(PATH)) {
            final JsonParser jsonParser = new JsonParser();
            final JsonElement parsedJson = jsonParser.parse(fileReader);

            final JsonElement logElement = parsedJson.getAsJsonObject().get("log");

            final JsonArray entries = logElement.getAsJsonObject().get("entries").getAsJsonArray();


            final Set<Download> downloads = StreamSupport.stream(entries.spliterator(), false)
                    .flatMap(e -> handleEntry(e.getAsJsonObject()).stream())
                    .collect(Collectors.toCollection(() -> new TreeSet<>()));

            downloads.stream().forEach(d -> System.out.println(d));


            printRuntimeDownloads(downloads);
            printRuntimeOverTimeDownloads(downloads);
            printUpdateEndpointRequests(downloads);
            printJvmUpdateEndpointRequests(downloads);
            printUpdateDownloads(downloads);
            printUpdateDownloadsOverTime(downloads);
        }
    }

    private static void printUpdateDownloads(final Set<Download> downloads) {
        System.out.println("-----------------------");
        System.out.println("- OWS Update Download -");
        System.out.println("-----------------------");

        final int updates041 = getSumForFilter((Set<Download>) downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "0_4_1");

        final int updates042 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "0_4_2");

        final int updates045 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "0_4_5");

        final int updates046 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "0_4_6");

        final int updates051 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "0_5_1");

        final int updates100 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_0_0");

        final int updates110 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_0");

        final int updates111 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_1");

        final int updates112 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_2");

        final int updates113 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_3");

        final int updates114 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_4");

        final int updates115 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_5");

        final int updates116 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_6");

        final int updates117 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "1_1_7");

        final int updatesWin32 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "windows-x32");

        final int updatesWin64 = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "windows-x64");

        final int updatesMac = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "macos");

        final int updatesLinux = getSumForFilter(downloads, "http://www.download-openwebstart.com/updates/OpenWebStart", "linux");

        System.out.println("Update 0.4.1 : " + updates041);
        System.out.println("Update 0.4.2 : " + updates042);
        System.out.println("Update 0.4.5 : " + updates045);
        System.out.println("Update 0.4.6 : " + updates046);
        System.out.println("Update 0.5.1 : " + updates051);
        System.out.println("Update 1.0.0 : " + updates100);
        System.out.println("Update 1.1.0 : " + updates110);
        System.out.println("Update 1.1.1 : " + updates111);
        System.out.println("Update 1.1.2 : " + updates112);
        System.out.println("Update 1.1.3 : " + updates113);
        System.out.println("Update 1.1.4 : " + updates114);
        System.out.println("Update 1.1.5 : " + updates115);
        System.out.println("Update 1.1.6 : " + updates116);
        System.out.println("Update 1.1.7 : " + updates117);

        System.out.println("Updates Win32 : " + updatesWin32);
        System.out.println("Updates Win64 : " + updatesWin64);
        System.out.println("Updates Mac : " + updatesMac);
        System.out.println("Updates Linux : " + updatesLinux);

        System.out.println("Total Updates: " + (updates041 + updates042 + updates045 + updates046 + updates051 + updates100 + updates110 + updates111 + updates112 + updates113 + updates114 + updates115 + updates116 + updates117));
    }

    private static void printUpdateDownloadsOverTime(final Set<Download> downloads) {
        System.out.println("-------------------------");
        System.out.println("- OWS updates over time -");
        System.out.println("-------------------------");

        final Map<String, Integer> downloadOverTime = new HashMap<>();

        downloads.stream()
                .filter(d -> d.getUrl().startsWith("http://www.download-openwebstart.com/updates/OpenWebStart"))
                .forEach(d -> {
                    final int lastCount = downloadOverTime.computeIfAbsent(d.getDate(), v -> 0);
                    downloadOverTime.put(d.getDate(), lastCount + d.getCount());
                });

        downloadOverTime.keySet().forEach(k -> System.out.println(k + " -> " + downloadOverTime.get(k)));
    }

    private static int getSumForFilter(final Set<Download> downloads, final String urlStart, final String urlContent) {
        return downloads.stream()
                .filter(d -> d.getUrl().startsWith(urlStart))
                .filter(d -> d.getUrl().contains(urlContent))
                .mapToInt(d -> d.getCount())
                .sum();
    }


    private static void printJvmUpdateEndpointRequests(final Set<Download> downloads) {
        System.out.println("-------------------------------------");
        System.out.println("- Runtime update requests over time -");
        System.out.println("-------------------------------------");

        final Map<String, Integer> downloadOverTime = new HashMap<>();

        downloads.stream()
                .filter(d -> d.getUrl().equals("http://www.download-openwebstart.com/jvms"))
                .forEach(d -> {
                    final int lastCount = downloadOverTime.computeIfAbsent(d.getDate(), v -> 0);
                    downloadOverTime.put(d.getDate(), lastCount + d.getCount());
                });

        downloadOverTime.keySet().forEach(k -> System.out.println(k + " -> " + downloadOverTime.get(k)));
    }

    private static void printUpdateEndpointRequests(final Set<Download> downloads) {
        System.out.println("---------------------------------");
        System.out.println("- OWS update requests over time -");
        System.out.println("---------------------------------");

        final Map<String, Integer> downloadOverTime = new HashMap<>();

        downloads.stream()
                .filter(d -> d.getUrl().equals("http://www.download-openwebstart.com/updates/updates.xml"))
                .forEach(d -> {
                    final int lastCount = downloadOverTime.computeIfAbsent(d.getDate(), v -> 0);
                    downloadOverTime.put(d.getDate(), lastCount + d.getCount());
                });

        downloadOverTime.keySet().forEach(k -> System.out.println(k + " -> " + downloadOverTime.get(k)));
    }

    private static void printRuntimeOverTimeDownloads(final Set<Download> downloads) {
        System.out.println("------------------------------");
        System.out.println("- Runtime Download over time -");
        System.out.println("------------------------------");

        final Map<String, Integer> downloadOverTime = new HashMap<>();

        downloads.stream()
                .filter(d -> d.getUrl().startsWith("http://www.download-openwebstart.com/AdoptOpenJDK/"))
                .forEach(d -> {
                    final int lastCount = downloadOverTime.computeIfAbsent(d.getDate(), v -> 0);
                    downloadOverTime.put(d.getDate(), lastCount + d.getCount());
                });

        downloads.stream()
                .filter(d -> d.getUrl().startsWith("http://www.download-openwebstart.com/Azul/"))
                .forEach(d -> {
                    final int lastCount = downloadOverTime.computeIfAbsent(d.getDate(), v -> 0);
                    downloadOverTime.put(d.getDate(), lastCount + d.getCount());
                });

        downloadOverTime.keySet().forEach(k -> System.out.println(k + " -> " + downloadOverTime.get(k)));
    }

    private static void printRuntimeDownloads(final Set<Download> downloads) {
        System.out.println("--------------------");
        System.out.println("- Runtime Download -");
        System.out.println("--------------------");

        final int totalAdoptDownloads = downloads.stream()
                .filter(d -> d.getUrl().startsWith("http://www.download-openwebstart.com/AdoptOpenJDK/"))
                .mapToInt(d -> d.getCount())
                .sum();

        final int totalAdopt11Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/AdoptOpenJDK/", "11.0.");

        final int totalAdopt8Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/AdoptOpenJDK/", "8u");

        final int totalAdoptWin32Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/AdoptOpenJDK/", "x86-32_windows");

        final int totalAdoptWin64Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/AdoptOpenJDK/", "x64_windows");

        final int totalAdoptMacDownloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/AdoptOpenJDK/", "mac");

        final int totalAdoptLinuxDownloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/AdoptOpenJDK/", "linux");


        System.out.println("Total Adopt: " + totalAdoptDownloads);
        System.out.println("Total Adopt 8: " + totalAdopt8Downloads);
        System.out.println("Total Adopt 11: " + totalAdopt11Downloads);

        final int totalAzulDownloads = downloads.stream()
                .filter(d -> d.getUrl().startsWith("http://www.download-openwebstart.com/Azul/"))
                .mapToInt(d -> d.getCount())
                .sum();

        final int totalAzul11Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "zulu11");

        final int totalAzul8Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "zulu8");

        final int totalAzulWin32Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "win_i686");

        final int totalAzulWin64Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "win_x64");

        final int totalAzulMacDownloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "macosx_x64");

        final int totalAzulLinux32Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "linux_i686");

        final int totalAzulLinux64Downloads = getSumForFilter(downloads, "http://www.download-openwebstart.com/Azul/", "linux_x64");

        System.out.println("Total Azul: " + totalAzulDownloads);
        System.out.println("Total Azul 8: " + totalAzul8Downloads);
        System.out.println("Total Azul 11: " + totalAzul11Downloads);

        System.out.println("Total win32: " + (totalAdoptWin32Downloads + totalAzulWin32Downloads));
        System.out.println("Total win64: " + (totalAdoptWin64Downloads + totalAzulWin64Downloads));
        System.out.println("Total mac: " + (totalAdoptMacDownloads + totalAzulMacDownloads));
        System.out.println("Total linux: " + (totalAdoptLinuxDownloads + totalAzulLinux32Downloads + totalAzulLinux64Downloads));

        System.out.println("Total Runtime: " + (totalAdoptDownloads + totalAzulDownloads));
    }

    private static Set<Download> handleEntry(JsonObject entry) {
        final JsonArray query = entry.get("request").getAsJsonObject().get("queryString").getAsJsonArray();
        final String ampDate = StreamSupport.stream(query.spliterator(), false)
                .map(e -> e.getAsJsonObject())
                .filter(o -> Objects.equals(o.get("name").getAsString(), "amp;date"))
                .map(o -> o.get("value").getAsString())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Amp-Date"));

        final String content = entry.get("response").getAsJsonObject().get("content").getAsJsonObject().get("text").getAsString();

        final JsonParser jsonParser = new JsonParser();
        final JsonElement contentElement = jsonParser.parse(content);
        final JsonObject toplist = contentElement.getAsJsonObject().get("toplist").getAsJsonObject();

        final JsonArray listArray = toplist.get("list").getAsJsonArray();
        final JsonArray valuesArray = toplist.get("values").getAsJsonArray();

        return IntStream.range(0, listArray.size())
                .mapToObj(i -> new Download(listArray.get(i).getAsString(), ampDate, valuesArray.get(i).getAsInt()))
                .collect(Collectors.toSet());
    }

    private static class Download implements Comparable<Download> {

        private final String url;

        private final String date;

        private final int count;

        public Download(final String url, final String date, final int count) {
            this.url = url;
            this.date = date;
            this.count = count;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Download download = (Download) o;
            return count == download.count &&
                    Objects.equals(url, download.url) &&
                    Objects.equals(date, download.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, date, count);
        }

        @Override
        public int compareTo(final Download o) {
            if (o == null) {
                return -1;
            }
            final Comparator<Download> dateComparator = Comparator.comparing(d -> d.getDate());
            final Comparator<Download> urlComparator = Comparator.comparing(d -> d.getUrl());
            final Comparator<Download> countComparator = Comparator.comparingInt(d -> d.getCount());
            return dateComparator.thenComparing(urlComparator).thenComparing(countComparator).compare(this, o);
        }

        public String getUrl() {
            return url;
        }

        public String getDate() {
            return date;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return date + ";" + url + ";" + count;
        }
    }
}
