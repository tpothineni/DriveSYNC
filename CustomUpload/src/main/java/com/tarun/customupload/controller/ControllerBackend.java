package com.tarun.customupload.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ControllerBackend {

    @GetMapping("/")
    public String home() {
        return "Welcome to the OAuth2.0 Secured Backend!";
    }

    @GetMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }
}
