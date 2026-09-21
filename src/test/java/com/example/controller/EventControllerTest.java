package com.example.controller;

import com.example.exception.EventNotFoundException;
import com.example.model.Event;
import com.example.repository.EventRepo;
import com.example.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.support.ErrorResponseMatchers.errorResponse;
import static com.example.support.EventFixtures.loginEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@WebMvcTest(EventController.class)
class EventControllerTest {

    private static final String LOGIN_JSON = """
            {"eventType":"LOGIN","eventDescription":"user login","eventOwner":"auth",
             "isActive":"Y","critical":"N","isReusable":"Y","gifStage":"STAGE_1"}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService service;

    // EventController injects an EventRepo it never uses, so the web slice needs a bean for it.
    @MockitoBean
    private EventRepo repo;

    @Test
    void getAllEventsReturnsEventsAsJson() throws Exception {
        when(service.getAllEvents()).thenReturn(List.of(loginEvent()));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + LOGIN_JSON + "]"));
    }

    @Test
    void getAllEventsReturnsEmptyArrayWhenNoneExist() throws Exception {
        when(service.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void findByEventTypeReturnsEvent() throws Exception {
        when(service.getEventByEventType("LOGIN")).thenReturn(loginEvent());

        mockMvc.perform(get("/events/{eventtype}", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(content().json(LOGIN_JSON));
    }

    @Test
    void findByEventTypeReturns404WhenMissing() throws Exception {
        when(service.getEventByEventType("MISSING")).thenThrow(new EventNotFoundException("MISSING"));

        mockMvc.perform(get("/events/{eventtype}", "MISSING"))
                .andExpect(errorResponse(NOT_FOUND, "Event not found: MISSING"));
    }

    @Test
    void updateEventReturns404WhenMissing() throws Exception {
        when(service.updateTaskByEventType(eq("MISSING"), any(Event.class)))
                .thenThrow(new EventNotFoundException("MISSING"));

        mockMvc.perform(put("/events/{eventtype}", "MISSING").contentType(APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(errorResponse(NOT_FOUND, "Event not found: MISSING"));
    }

    @Test
    void deleteEventReturns404WhenMissing() throws Exception {
        doThrow(new EventNotFoundException("MISSING")).when(service).deleteEventByEventType("MISSING");

        mockMvc.perform(delete("/events/{eventtype}", "MISSING"))
                .andExpect(errorResponse(NOT_FOUND, "Event not found: MISSING"));
    }

    @Test
    void addEventBindsRequestJsonAndReturnsSavedEvent() throws Exception {
        when(service.addEvent(any(Event.class))).thenReturn(loginEvent());

        mockMvc.perform(post("/events").contentType(APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(LOGIN_JSON));

        ArgumentCaptor<Event> saved = ArgumentCaptor.forClass(Event.class);
        verify(service).addEvent(saved.capture());
        assertThat(saved.getValue()).usingRecursiveComparison().isEqualTo(loginEvent());
    }

    @Test
    void addEventRejectsBlankEventTypeWithoutEchoingRejectedValues() throws Exception {
        String body = mockMvc.perform(post("/events").contentType(APPLICATION_JSON)
                        .content("{\"eventType\":\"   \",\"eventDescription\":\"secret-looking-value\"}"))
                .andExpect(errorResponse(BAD_REQUEST, "Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("eventType: must not be blank"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("secret-looking-value");
        verify(service, never()).addEvent(any());
    }

    @Test
    void addEventRejectsMalformedJsonWithoutParserDetails() throws Exception {
        String body = mockMvc.perform(post("/events").contentType(APPLICATION_JSON).content("{ not valid json"))
                .andExpect(errorResponse(BAD_REQUEST, "Malformed request body"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("not valid json").doesNotContain("jackson");
    }

    @Test
    void addEventRejectsEmptyBody() throws Exception {
        mockMvc.perform(post("/events").contentType(APPLICATION_JSON))
                .andExpect(errorResponse(BAD_REQUEST, "Malformed request body"));
    }

    @Test
    void addEventRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/events").contentType(TEXT_PLAIN).content("plain text"))
                .andExpect(errorResponse(UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    void updateEventBindsRequestJsonAndReturnsUpdatedEvent() throws Exception {
        when(service.updateTaskByEventType(eq("LOGIN"), any(Event.class))).thenReturn(loginEvent());

        mockMvc.perform(put("/events/{eventtype}", "LOGIN").contentType(APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(LOGIN_JSON));

        ArgumentCaptor<Event> update = ArgumentCaptor.forClass(Event.class);
        verify(service).updateTaskByEventType(eq("LOGIN"), update.capture());
        assertThat(update.getValue()).usingRecursiveComparison().isEqualTo(loginEvent());
    }

    @Test
    void deleteEventDelegatesToService() throws Exception {
        mockMvc.perform(delete("/events/{eventtype}", "LOGIN"))
                .andExpect(status().isOk());

        verify(service).deleteEventByEventType("LOGIN");
    }

    @Test
    void unsupportedHttpMethodReturnsStandardErrorBody() throws Exception {
        mockMvc.perform(patch("/events/{eventtype}", "LOGIN"))
                .andExpect(errorResponse(METHOD_NOT_ALLOWED));
    }

    @Test
    void unknownRouteReturnsStandardErrorBody() throws Exception {
        mockMvc.perform(get("/no-such-route"))
                .andExpect(errorResponse(NOT_FOUND));
    }

    @Test
    void unexpectedFailureReturnsGenericErrorWithoutInternals() throws Exception {
        when(service.getAllEvents()).thenThrow(new RuntimeException("jdbc:postgresql://db/events?password=secret"));

        String body = mockMvc.perform(get("/events"))
                .andExpect(errorResponse(INTERNAL_SERVER_ERROR, "Unexpected error"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("secret").doesNotContain("jdbc").doesNotContain("RuntimeException");
    }

    @Test
    void unexpectedFailureIsLoggedOnceWithStackTrace(CapturedOutput output) throws Exception {
        when(service.getAllEvents()).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/events"))
                .andExpect(errorResponse(INTERNAL_SERVER_ERROR, "Unexpected error"));

        assertThat(output.getAll().split("Unexpected error on GET /events", -1)).hasSize(2);
        assertThat(output.getAll()).contains("java.lang.IllegalStateException: boom");
    }

    @Test
    void requestFromAllowedOriginGetsCorsHeader() throws Exception {
        when(service.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events").header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void requestFromDisallowedOriginIsRejectedBeforeReachingTheService() throws Exception {
        mockMvc.perform(get("/events").header("Origin", "http://evil.example.com"))
                .andExpect(status().isForbidden());

        verify(service, never()).getAllEvents();
    }
}
