package com.oddiya.auth.controller;

import com.oddiya.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @环境的
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testAuthorizeGoogleEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/oauth2/authorize/google")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testJwksEndpointExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/oauth2/.well-known/jwks.json")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}

