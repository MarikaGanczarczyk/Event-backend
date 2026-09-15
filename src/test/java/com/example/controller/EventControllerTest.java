package com.example.controller;

import com.example.exception.EventNotFoundException;
import com.example.model.Event;
import com.example.repository.EventRepo;
import com.example.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService service;

    @MockitoBean
    private EventRepo repo;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setEventType("LOGIN");
        event.setEventDescription("user login");
        event.setEventOwner("auth");
        event.setIsActive('Y');
        event.setCritical('N');
        event.setIsReusable('Y');
        event.setGifStage("STAGE_1");
    }

    @Test
    void getAllEventsReturnsJsonArray() throws Exception {
        when(service.getAllEvents()).thenReturn(List.of(event));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eventType").value("LOGIN"))
                .andExpect(jsonPath("$[0].eventDescription").value("user login"));
    }

    @Test
    void findByEventTypeReturnsEvent() throws Exception {
        when(service.getEventByEventType("LOGIN")).thenReturn(event);

        mockMvc.perform(get("/events/{eventtype}", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("LOGIN"));
    }

    @Test
    void addEventReturnsSavedEvent() throws Exception {
        when(service.addEvent(any(Event.class))).thenReturn(event);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("LOGIN"));

        verify(service).addEvent(any(Event.class));
    }

    @Test
    void addEventRejectsBlankEventType() throws Exception {
        Event invalid = new Event();
        invalid.setEventDescription("no type");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventByEventTypeReturnsUpdated() throws Exception {
        when(service.updateTaskByEventType(eq("LOGIN"), any(Event.class))).thenReturn(event);

        mockMvc.perform(put("/events/{eventtype}", "LOGIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventDescription").value("user login"));
    }

    @Test
    void deleteEventByEventTypeReturnsOk() throws Exception {
        mockMvc.perform(delete("/events/{eventtype}", "LOGIN"))
                .andExpect(status().isOk());

        verify(service).deleteEventByEventType("LOGIN");
    }

    @Test
    void getAllEventsReturnsEmptyArrayWhenNoneExist() throws Exception {
        when(service.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void findByEventTypeReturns404WhenMissing() throws Exception {
        when(service.getEventByEventType("MISSING")).thenThrow(new EventNotFoundException("MISSING"));

        mockMvc.perform(get("/events/{eventtype}", "MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found: MISSING"));
    }

    @Test
    void updateEventByEventTypeReturns404WhenMissing() throws Exception {
        when(service.updateTaskByEventType(eq("MISSING"), any(Event.class)))
                .thenThrow(new EventNotFoundException("MISSING"));

        mockMvc.perform(put("/events/{eventtype}", "MISSING")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found: MISSING"));
    }

    @Test
    void deleteEventByEventTypeReturns404WhenMissing() throws Exception {
        org.mockito.Mockito.doThrow(new EventNotFoundException("MISSING"))
                .when(service).deleteEventByEventType("MISSING");

        mockMvc.perform(delete("/events/{eventtype}", "MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found: MISSING"));
    }

    @Test
    void addEventRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEventRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text body"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void getAllEventsIncludesCorsHeaderForAllowedOrigin() throws Exception {
        when(service.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events").header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void getAllEventsRejectsDisallowedOriginBeforeReachingController() throws Exception {
        mockMvc.perform(get("/events").header("Origin", "http://evil.example.com"))
                .andExpect(status().isForbidden());

        verify(service, org.mockito.Mockito.never()).getAllEvents();
    }
}
