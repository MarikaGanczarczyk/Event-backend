package com.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorsConfigTest {

    private CorsConfig config;
    private CorsRegistry registry;
    private CorsRegistration registration;

    @BeforeEach
    void setUp() {
        config = new CorsConfig();
        registry = mock(CorsRegistry.class);
        registration = mock(CorsRegistration.class);

        when(registry.addMapping("/**")).thenReturn(registration);
        when(registration.allowedOrigins("http://localhost:3000")).thenReturn(registration);
        when(registration.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"))
                .thenReturn(registration);
        when(registration.allowedHeaders("*")).thenReturn(registration);
    }

    @Test
    void addCorsMappingsRegistersExpectedPolicy() {
        config.addCorsMappings(registry);

        verify(registry).addMapping("/**");
        verify(registration).allowedOrigins("http://localhost:3000");
        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        verify(registration).allowedHeaders("*");
    }

    @Test
    void classImplementsWebMvcConfigurer() {
        org.assertj.core.api.Assertions.assertThat(config)
                .isInstanceOf(org.springframework.web.servlet.config.annotation.WebMvcConfigurer.class);
    }
}
