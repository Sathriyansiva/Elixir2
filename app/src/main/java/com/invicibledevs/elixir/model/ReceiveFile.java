package com.invicibledevs.elixir.model;

/**
 * Created by nandhakumarv on 28/09/16.
 */
public class ReceiveFile {

    private String senderName;
    private String message;
    private String date;
    private String fileName;
    private String downloadedPath;
    private boolean isDownloaded;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public String getDownloadedPath() {
        return downloadedPath;
    }

    public void setDownloadedPath(String downloadedPath) {
        this.downloadedPath = downloadedPath;
    }
}
