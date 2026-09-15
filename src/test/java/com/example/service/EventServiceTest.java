package com.example.service;

import com.example.model.Event;
import com.example.repository.EventRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepo repo;

    @InjectMocks
    private EventService service;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setEventType("LOGIN");
        event.setEventDescription("desc");
        event.setEventOwner("owner");
        event.setIsActive('Y');
        event.setCritical('N');
        event.setIsReusable('Y');
        event.setGifStage("STAGE_1");
    }

    @Test
    void getAllEventsReturnsRepoList() {
        when(repo.findAll()).thenReturn(List.of(event));

        List<Event> result = service.getAllEvents();

        assertThat(result).containsExactly(event);
        verify(repo).findAll();
    }

    @Test
    void getEventByEventTypeReturnsEventWhenPresent() {
        when(repo.findByEventType("LOGIN")).thenReturn(Optional.of(event));

        Event result = service.getEventByEventType("LOGIN");

        assertThat(result).isSameAs(event);
    }

    @Test
    void getEventByEventTypeThrowsWhenMissing() {
        when(repo.findByEventType("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEventByEventType("NOPE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NOPE");
    }

    @Test
    void addEventDelegatesToRepoSave() {
        when(repo.save(event)).thenReturn(event);

        Event result = service.addEvent(event);

        assertThat(result).isSameAs(event);
        verify(repo).save(event);
    }

    @Test
    void updateTaskByEventTypeCopiesMutableFieldsAndSaves() {
        Event updated = new Event();
        updated.setEventType("IGNORED");
        updated.setEventDescription("new desc");
        updated.setEventOwner("new owner");
        updated.setIsActive('N');
        updated.setCritical('Y');
        updated.setIsReusable('N');
        updated.setGifStage("STAGE_9");

        when(repo.findByEventType("LOGIN")).thenReturn(Optional.of(event));
        when(repo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = service.updateTaskByEventType("LOGIN", updated);

        assertThat(result.getEventType()).isEqualTo("LOGIN");
        assertThat(result.getEventDescription()).isEqualTo("new desc");
        assertThat(result.getEventOwner()).isEqualTo("new owner");
        assertThat(result.getIsActive()).isEqualTo('N');
        assertThat(result.getCritical()).isEqualTo('Y');
        assertThat(result.getIsReusable()).isEqualTo('N');
        assertThat(result.getGifStage()).isEqualTo("STAGE_9");
        verify(repo).save(event);
    }

    @Test
    void updateTaskByEventTypeThrowsWhenMissing() {
        when(repo.findByEventType("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTaskByEventType("NOPE", new Event()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NOPE");
        verify(repo, never()).save(any());
    }

    @Test
    void deleteEventByEventTypeDelegatesToRepo() {
        service.deleteEventByEventType("LOGIN");

        verify(repo).deleteById("LOGIN");
    }

    @Test
    void deleteEventByEventTypePropagatesRepoExceptionWhenMissing() {
        doThrow(new EmptyResultDataAccessException(1)).when(repo).deleteById("NOPE");

        assertThatThrownBy(() -> service.deleteEventByEventType("NOPE"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void updateTaskByEventTypeNullsOutFieldsThatAreNullOnTheUpdate() {
        Event blankUpdate = new Event();

        when(repo.findByEventType("LOGIN")).thenReturn(Optional.of(event));
        when(repo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = service.updateTaskByEventType("LOGIN", blankUpdate);

        assertThat(result.getEventDescription()).isNull();
        assertThat(result.getEventOwner()).isNull();
        assertThat(result.getIsActive()).isNull();
        assertThat(result.getCritical()).isNull();
        assertThat(result.getIsReusable()).isNull();
        assertThat(result.getGifStage()).isNull();
    }
}
