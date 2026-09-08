package com.example.repository;

import com.example.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepo extends JpaRepository<Event, String> {


    Optional<Event> findByEventType(String eventType);


}
