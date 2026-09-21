package com.example.repository;

import com.example.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static com.example.support.EventFixtures.eventOfType;
import static com.example.support.EventFixtures.loginEvent;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepoTest {

    @Autowired
    private EventRepo repo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEventTypeReturnsPersistedEventWithEveryFieldMapped() {
        Event event = loginEvent();
        event.setCreatedDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        event.setUpdatedDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        event.setLastUpdatedBy("erik");
        entityManager.persistAndFlush(event);
        entityManager.clear();

        assertThat(repo.findByEventType("LOGIN")).get().usingRecursiveComparison().isEqualTo(event);
    }

    // Documents current behaviour: eventType is an assigned id, so saving an existing eventType replaces the row (upsert).
    @Test
    void saveWithExistingEventTypeReplacesTheStoredEvent() {
        entityManager.persistAndFlush(loginEvent());
        entityManager.clear();

        Event replacement = loginEvent();
        replacement.setEventDescription("replacement");
        repo.saveAndFlush(replacement);
        entityManager.clear();

        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.findByEventType("LOGIN")).get().extracting(Event::getEventDescription).isEqualTo("replacement");
    }

    @Test
    void findByEventTypeReturnsEmptyWhenNoEventHasThatType() {
        entityManager.persistAndFlush(eventOfType("LOGOUT"));

        assertThat(repo.findByEventType("LOGIN")).isEmpty();
    }
}
