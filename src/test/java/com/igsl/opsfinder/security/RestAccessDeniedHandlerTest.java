package com.igsl.opsfinder.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that an authenticated-but-unauthorized request still receives 403, so it is
 * never mistaken for a session expiry and never triggers a token refresh or logout.
 */
class RestAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;
    private RestAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new RestAccessDeniedHandler(objectMapper);
    }

    @Test
    void handleReturns403WithJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access Denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("error").asString()).isEqualTo("Forbidden");
        assertThat(body.get("path").asString()).isEqualTo("/api/admin/users");
        assertThat(body.get("message").asString()).contains("permission");
    }
}
