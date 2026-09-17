package com.devops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devops.security.ApiKeyAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    private static final String VALID_API_KEY = "test-api-key-2f5ae96c";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void issueToken_withValidApiKey_returnsJwt() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    @Test
    void issueToken_withInvalidApiKey_returnsError() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void issueToken_withCustomTtl_isReflectedInResponse() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .param("ttlSeconds", "120")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresInSeconds").value(120));
    }
}
