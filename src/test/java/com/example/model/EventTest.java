package com.example.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.example.support.EventFixtures.eventOfType;
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void validationFailsForMissingOrBlankEventType(String eventType) {
        assertThat(validator.validate(eventOfType(eventType)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("eventType");
    }

    @Test
    void validationPassesWhenEventTypeIsPresent() {
        assertThat(validator.validate(eventOfType("LOGIN"))).isEmpty();
    }
}
