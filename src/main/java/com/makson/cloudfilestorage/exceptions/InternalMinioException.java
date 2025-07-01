package com.makson.cloudfilestorage.exceptions;

public class InternalMinioException extends RuntimeException {
    public InternalMinioException() {
        super();
    }

    public InternalMinioException(String message) {
        super(message);
    }

    public InternalMinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalMinioException(Throwable cause) {
        super(cause);
    }
}
