package com.makson.cloudfilestorage.exceptions;

public class ResourceDownloadException extends RuntimeException {
    public ResourceDownloadException() {
        super();
    }

    public ResourceDownloadException(String message) {
        super(message);
    }

    public ResourceDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceDownloadException(Throwable cause) {
        super(cause);
    }
}
