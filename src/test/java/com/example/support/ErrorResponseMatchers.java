package com.example.support;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ErrorResponseMatchers {

    private ErrorResponseMatchers() {
    }

    public static ResultMatcher errorResponse(HttpStatus expected) {
        return all(
                status().is(expected.value()),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.status").value(expected.value()),
                jsonPath("$.error").value(expected.getReasonPhrase()),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.timestamp").isString());
    }

    public static ResultMatcher errorResponse(HttpStatus expected, String message) {
        return all(errorResponse(expected), jsonPath("$.message").value(message));
    }

    private static ResultMatcher all(ResultMatcher... matchers) {
        return result -> {
            for (ResultMatcher matcher : matchers) {
                matcher.match(result);
            }
        };
    }
}
