package com.wetube.video_service.exception;

public class ResourceNotReadyException extends RuntimeException {
    public ResourceNotReadyException(String message) {
        super(message);
    }
}
