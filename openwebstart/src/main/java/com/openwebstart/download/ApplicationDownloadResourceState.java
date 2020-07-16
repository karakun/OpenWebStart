package com.openwebstart.download;

import java.net.URL;

class ApplicationDownloadResourceState {

    private final URL url;

    private final String version;

    private final String message;

    private final int percentage;

    private final ApplicationDownloadState downloadState;

    public ApplicationDownloadResourceState(final URL url, final String version, final String message, final int percentage, final ApplicationDownloadState downloadState) {
        this.url = url;
        this.version = version;
        this.message = message;
        this.percentage = percentage;
        this.downloadState = downloadState;
    }

    public String getMessage() {
        return message;
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
