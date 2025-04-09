package com.tarun.customupload.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Map;

/**
 *Provides API related to authentication, user Data  and CSRF Token
 */
@RestController
public class AuthController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the OAuth2 Secured Google Drive Backend!";
    }

    @GetMapping("/user")
    public Map<String, Object> getUserDetails(OAuth2AuthenticationToken auth) {
        return auth.getPrincipal().getAttributes();
    }

    @GetMapping("/csrf-token")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}