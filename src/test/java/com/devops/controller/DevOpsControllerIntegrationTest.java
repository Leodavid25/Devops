package com.devops.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devops.security.ApiKeyAuthFilter;
import com.devops.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevOpsControllerIntegrationTest {

    private static final String VALID_API_KEY = "test-api-key-2f5ae96c";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String validPayload() throws Exception {
        return objectMapper.writeValueAsString(new com.devops.dto.DevOpsRequest(
                "This is a test", "Juan Perez", "Rita Asturia", 45));
    }

    @Test
    void post_withValidApiKeyAndJwt_returnsExpectedGreeting() throws Exception {
        String jwt = jwtService.issueToken(45);

        mockMvc.perform(post("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY)
                        .header(DevOpsController.JWT_HEADER, jwt)
                        .contentType("application/json")
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Hello Juan Perez your message will be send")));
    }

    @Test
    void post_withoutApiKey_returnsError() throws Exception {
        String jwt = jwtService.issueToken(45);

        mockMvc.perform(post("/DevOps")
                        .header(DevOpsController.JWT_HEADER, jwt)
                        .contentType("application/json")
                        .content(validPayload()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void post_withWrongApiKey_returnsError() throws Exception {
        String jwt = jwtService.issueToken(45);

        mockMvc.perform(post("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, "wrong-key")
                        .header(DevOpsController.JWT_HEADER, jwt)
                        .contentType("application/json")
                        .content(validPayload()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void post_withReusedJwt_returnsErrorOnSecondCall() throws Exception {
        String jwt = jwtService.issueToken(45);

        mockMvc.perform(post("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY)
                        .header(DevOpsController.JWT_HEADER, jwt)
                        .contentType("application/json")
                        .content(validPayload()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY)
                        .header(DevOpsController.JWT_HEADER, jwt)
                        .contentType("application/json")
                        .content(validPayload()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void post_withMissingJwt_returnsError() throws Exception {
        mockMvc.perform(post("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(validPayload()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void get_onDevOpsPath_returnsError() throws Exception {
        mockMvc.perform(get("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void put_onDevOpsPath_returnsError() throws Exception {
        mockMvc.perform(put("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void delete_onDevOpsPath_returnsError() throws Exception {
        mockMvc.perform(delete("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void post_withInvalidPayload_returnsBadRequest() throws Exception {
        String jwt = jwtService.issueToken(45);
        String invalidPayload = "{\"message\":\"\",\"to\":\"\",\"from\":\"\",\"timeToLifeSec\":-1}";

        mockMvc.perform(post("/DevOps")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, VALID_API_KEY)
                        .header(DevOpsController.JWT_HEADER, jwt)
                        .contentType("application/json")
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
