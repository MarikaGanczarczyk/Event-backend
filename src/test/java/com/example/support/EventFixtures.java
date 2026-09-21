package com.example.support;

import com.example.model.Event;

public final class EventFixtures {

    private EventFixtures() {
    }

    public static Event loginEvent() {
        return eventOfType("LOGIN");
    }

    public static Event eventOfType(String eventType) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setEventDescription("user login");
        event.setEventOwner("auth");
        event.setIsActive('Y');
        event.setCritical('N');
        event.setIsReusable('Y');
        event.setGifStage("STAGE_1");
        return event;
    }
}
