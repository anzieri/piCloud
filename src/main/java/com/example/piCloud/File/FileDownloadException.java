package com.example.piCloud.File;

public class FileDownloadException extends RuntimeException {


    public FileDownloadException(Object source, String message, Throwable cause) {
        super(message, cause);
    }

    public FileDownloadException(String message) {
        super(message);
    }
}

