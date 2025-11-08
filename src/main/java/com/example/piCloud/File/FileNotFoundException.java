package com.example.piCloud.File;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }

    // Added constructors so existing call-sites that pass 'this' continue to work
    public FileNotFoundException(Object source, String message) {
        super(message);
    }

    public FileNotFoundException(Object source, String message, Throwable cause) {
        super(message, cause);
    }
}
