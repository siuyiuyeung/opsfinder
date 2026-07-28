package com.igsl.opsfinder.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that unauthenticated requests receive 401 (not the Spring default 403),
 * so clients can distinguish "refresh or re-login" from "not permitted".
 */
class JwtAuthenticationEntryPointTest {

    private ObjectMapper objectMapper;
    private JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Test
    void commenceReturns401WithJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/devices");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("expired"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("error").asString()).isEqualTo("Unauthorized");
        assertThat(body.get("path").asString()).isEqualTo("/api/devices");
        assertThat(body.get("message").asString()).contains("expired");
        assertThat(body.get("timestamp").asString()).isNotBlank();
    }
}
