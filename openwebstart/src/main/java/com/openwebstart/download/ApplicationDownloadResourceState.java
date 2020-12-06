package com.openwebstart.download;

import java.net.URL;

class ApplicationDownloadResourceState {

    private final URL url;

    private final String version;

    private final int percentage;

    private final ApplicationDownloadState downloadState;

    public ApplicationDownloadResourceState(final URL url, final String version, final int percentage, final ApplicationDownloadState downloadState) {
        this.url = url;
        this.version = version;
        this.percentage = percentage;
        this.downloadState = downloadState;
    }

    public int getPercentage() {
        return percentage;
    }

    public ApplicationDownloadState getDownloadState() {
        return downloadState;
    }

    public URL getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }
}
