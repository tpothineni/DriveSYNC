package com.tarun.customupload.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class AuthControllerTest {

    //Simulates HTTP requests
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        AuthController controller = new AuthController();

        //Builds the MockMvc around the AuthController without using Spring context
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to the OAuth2 Secured Google Drive Backend!"));
    }

    @Test
    void testGetUserDetails() throws Exception {
        // Create mock user attributes
        Map<String, Object> attributes = Map.of(
                "name", "Test user",
                "email", "testuser@example.com"
        );

        // Create an OAuth2User with the attributes
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email" // attribute key used for getName()
        );

        // Wrap it in an OAuth2AuthenticationToken
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "google"
        );

        mockMvc.perform(get("/user")
                        .principal(token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test user"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }
}
