package com.openwebstart.downloadcount;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openwebstart.http.HttpGetRequest;
import com.openwebstart.http.HttpResponse;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadCount {

    public static void main(String[] args) throws Exception {
        final List<Version> versions = new ArrayList<>();

        final URL downloadRequest = new URL("https://api.github.com/repos/karakun/OpenWebStart/releases");
        final HttpGetRequest request = new HttpGetRequest(downloadRequest);
        final HttpResponse response = request.handle();
        final String jsonResult = IOUtils.readContentAsUtf8String(response.getContentStream());
        final JsonParser jsonParser = new JsonParser();
        final JsonElement jsonElement = jsonParser.parse(jsonResult);
        jsonElement.getAsJsonArray().forEach(e -> {
            final JsonObject versionObject = e.getAsJsonObject();
            final String versionString = versionObject.get("tag_name").getAsString();
            final Version version = new Version(versionString);
            versions.add(version);
            versionObject.getAsJsonArray("assets").forEach(a -> {
                final JsonObject osVersion = a.getAsJsonObject();
                final String name = osVersion.get("name").getAsString();
                final int downloadCount = osVersion.get("download_count").getAsInt();
                final Artifact artifact = new Artifact(name, downloadCount);
                version.addArtifact(artifact);
            });
        });

        versions.forEach(v -> {
            System.out.println();
            System.out.println("---------------------------------------------");
            System.out.println("Version '" + v.getVersion() + "' -> " + v.getDownloadCount() + " Downloads");
            System.out.println("---------------------------------------------");
            v.getArtifacts().stream().sorted(Comparator.<DownloadCount.Artifact, String>comparing(a -> a.getOs()).reversed()).forEach(a -> {
                System.out.println("    OS '" + a.getOs() + "' -> " + a.getDownloadCount() + " Downloads");
            });
            System.out.println("---------------------------------------------");
        });

        System.out.println("Total: " + versions.stream().mapToLong(v -> v.getDownloadCount()).sum() + " Downloads");
    }

    private static class Artifact {

        private final String name;

        private final long downloadCount;

        public Artifact(final String name, final long downloadCount) {
            this.name = name;
            this.downloadCount = downloadCount;
        }

        public String getName() {
            return name;
        }

        public long getDownloadCount() {
            return downloadCount;
        }

        public String getOs() {
            if (name.contains("linux")) {
                return "Linux";
            }
            if (name.contains("macos")) {
                return "MacOs";
            }
            if (name.contains("windows-x32")) {
                return "Win32";
            }
            if (name.contains("windows-x64")) {
                return "Win64";
            }
            return "Unknown";
        }
    }

    private static class Version {

        private final String version;

        private final List<Artifact> artifacts = new ArrayList<>();

        public Version(final String version) {
            this.version = version;
        }

        public void addArtifact(final Artifact artifact) {
            artifacts.add(artifact);
        }

        public List<Artifact> getArtifacts() {
            return Collections.unmodifiableList(artifacts);
        }

        public String getVersion() {
            return version;
        }

        public long getDownloadCount() {
            return getArtifacts().stream().mapToLong(a -> a.getDownloadCount()).sum();
        }
    }
}
