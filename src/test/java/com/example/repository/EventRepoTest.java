package com.example.repository;

import com.example.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepoTest {

    @Autowired
    private EventRepo repo;

    @Autowired
    private TestEntityManager entityManager;

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
        event.setCreatedDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        event.setUpdatedDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        event.setLastUpdatedBy("erik");
    }

    @Test
    void savePersistsEvent() {
        Event saved = repo.save(event);

        assertThat(saved.getEventType()).isEqualTo("LOGIN");
        assertThat(entityManager.find(Event.class, "LOGIN")).isNotNull();
    }

    @Test
    void findByEventTypeReturnsPersistedEvent() {
        entityManager.persistAndFlush(event);

        Optional<Event> found = repo.findByEventType("LOGIN");

        assertThat(found).isPresent();
        assertThat(found.get().getEventDescription()).isEqualTo("user login");
        assertThat(found.get().getIsActive()).isEqualTo('Y');
    }

    @Test
    void findByEventTypeReturnsEmptyWhenMissing() {
        assertThat(repo.findByEventType("MISSING")).isEmpty();
    }

    @Test
    void findAllReturnsEveryRow() {
        entityManager.persistAndFlush(event);

        Event other = new Event();
        other.setEventType("LOGOUT");
        entityManager.persistAndFlush(other);

        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void deleteByIdRemovesEvent() {
        entityManager.persistAndFlush(event);

        repo.deleteById("LOGIN");
        entityManager.flush();

        assertThat(entityManager.find(Event.class, "LOGIN")).isNull();
    }

    @Test
    void saveWithExistingEventTypeUpdatesInsteadOfDuplicating() {
        entityManager.persistAndFlush(event);

        Event sameId = new Event();
        sameId.setEventType("LOGIN");
        sameId.setEventDescription("updated description");
        repo.save(sameId);
        entityManager.flush();

        assertThat(repo.findAll()).hasSize(1);
        assertThat(entityManager.find(Event.class, "LOGIN").getEventDescription())
                .isEqualTo("updated description");
    }
}
