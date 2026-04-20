package edu.tcu.projectpulse.api;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
