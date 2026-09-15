package com.example.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(String eventType) {
        super("Event not found: " + eventType);
    }
}
