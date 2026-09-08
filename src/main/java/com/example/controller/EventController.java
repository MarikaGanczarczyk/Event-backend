package com.example.controller;


import com.example.model.Event;
import com.example.repository.EventRepo;
import com.example.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class EventController {


@Autowired
private EventRepo repo;
@Autowired
private EventService service;


//Get /events
@GetMapping("/events")
    public List<Event> getAllEvents (){
    return service.getAllEvents();
}

//Get /events/{eventtype}
    @GetMapping("/events/{eventtype}")
    public ResponseEntity<Event> findByEventType(@PathVariable("eventtype") String eventType){
     Event event = service.getEventByEventType(eventType);
     return ResponseEntity.ok(event);
    }


 //POST /events
    @PostMapping("/events")
    public Event addEvent(@Valid @RequestBody Event event){return service.addEvent(event);}


//PUT /events/${id}
@PutMapping("/events/{eventtype}")
    public ResponseEntity<Event> updateEventByEventType(@PathVariable("eventtype") String eventType, @RequestBody Event updatedEvent){
    Event event = service.updateTaskByEventType(eventType, updatedEvent);
    return ResponseEntity.ok(event);
}

//DELETE /events/{eventtype}
    @DeleteMapping("/events/{eventtype}")
    public void deleteEventByEventType(@PathVariable("eventtype") String eventType){service.deleteEventByEventType(eventType);}
}
