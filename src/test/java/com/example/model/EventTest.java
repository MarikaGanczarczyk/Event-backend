package com.example.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void noArgsConstructorCreatesEmptyEvent() {
        Event event = new Event();
        assertThat(event.getEventType()).isNull();
        assertThat(event.getEventDescription()).isNull();
    }

    @Test
    void allArgsConstructorSetsEveryField() {
        LocalDateTime updated = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 10, 0);

        Event event = new Event(
                "LOGIN",
                "user login event",
                "auth-team",
                'Y',
                'N',
                'Y',
                "STAGE_1",
                updated,
                created,
                "erik");

        assertThat(event.getEventType()).isEqualTo("LOGIN");
        assertThat(event.getEventDescription()).isEqualTo("user login event");
        assertThat(event.getEventOwner()).isEqualTo("auth-team");
        assertThat(event.getIsActive()).isEqualTo('Y');
        assertThat(event.getCritical()).isEqualTo('N');
        assertThat(event.getIsReusable()).isEqualTo('Y');
        assertThat(event.getGifStage()).isEqualTo("STAGE_1");
        assertThat(event.getUpdatedDate()).isEqualTo(updated);
        assertThat(event.getCreatedDate()).isEqualTo(created);
        assertThat(event.getLastUpdatedBy()).isEqualTo("erik");
    }

    @Test
    void settersAndGettersRoundTrip() {
        Event event = new Event();
        event.setEventType("LOGOUT");
        event.setEventDescription("desc");
        event.setEventOwner("owner");
        event.setIsActive('N');
        event.setCritical('Y');
        event.setIsReusable('N');
        event.setGifStage("STAGE_2");
        LocalDateTime now = LocalDateTime.now();
        event.setUpdatedDate(now);
        event.setCreatedDate(now);
        event.setLastUpdatedBy("someone");

        assertThat(event.getEventType()).isEqualTo("LOGOUT");
        assertThat(event.getEventDescription()).isEqualTo("desc");
        assertThat(event.getEventOwner()).isEqualTo("owner");
        assertThat(event.getIsActive()).isEqualTo('N');
        assertThat(event.getCritical()).isEqualTo('Y');
        assertThat(event.getIsReusable()).isEqualTo('N');
        assertThat(event.getGifStage()).isEqualTo("STAGE_2");
        assertThat(event.getUpdatedDate()).isEqualTo(now);
        assertThat(event.getCreatedDate()).isEqualTo(now);
        assertThat(event.getLastUpdatedBy()).isEqualTo("someone");
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        Event a = new Event();
        a.setEventType("X");
        a.setEventDescription("d");

        Event b = new Event();
        b.setEventType("X");
        b.setEventDescription("d");

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);

        b.setEventDescription("different");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringContainsFieldValues() {
        Event event = new Event();
        event.setEventType("PING");
        assertThat(event.toString()).contains("PING");
    }

    @Test
    void validationFailsWhenEventTypeIsBlank() {
        Event event = new Event();
        event.setEventType("   ");

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("eventType"));
    }

    @Test
    void validationFailsWhenEventTypeIsNull() {
        Event event = new Event();

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("eventType"));
    }

    @Test
    void validationPassesWhenEventTypeIsPresent() {
        Event event = new Event();
        event.setEventType("LOGIN");

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertThat(violations).isEmpty();
    }
}
