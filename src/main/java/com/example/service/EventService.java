package com.example.service;


import com.example.exception.EventNotFoundException;
import com.example.model.Event;
import com.example.repository.EventRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepo repo;

    public List<Event> getAllEvents (){
        return repo.findAll();
    }


    public Event getEventByEventType(String eventType){
        return repo.findByEventType(eventType)
                .orElseThrow(()-> new EventNotFoundException(eventType));
    }

    public Event addEvent(Event event){return repo.save(event);}

    public Event updateTaskByEventType(String eventType, Event updatedEvent) {

        Event existingEvent = repo.findByEventType(eventType).orElseThrow(()-> new EventNotFoundException(eventType));
//        existingEvent.setEventType(updatedEvent.getEventType());
        existingEvent.setEventDescription(updatedEvent.getEventDescription());
        existingEvent.setEventOwner(updatedEvent.getEventOwner());
        existingEvent.setIsActive(updatedEvent.getIsActive());
        existingEvent.setCritical(updatedEvent.getCritical());
        existingEvent.setIsReusable(updatedEvent.getIsReusable());
        existingEvent.setGifStage(updatedEvent.getGifStage());
        return repo.save(existingEvent);

    }

    public void deleteEventByEventType(String eventType){
        if (!repo.existsById(eventType)) {
            throw new EventNotFoundException(eventType);
        }
        repo.deleteById(eventType);
    }
}
