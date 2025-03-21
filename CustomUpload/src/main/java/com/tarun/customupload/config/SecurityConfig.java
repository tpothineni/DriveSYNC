package com.tarun.customupload.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.tarun.customupload.CustomSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomSuccessHandler customSuccessHandler) throws Exception {
        http
                // Allow preflight OPTIONS requests for CORS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/public").permitAll()
                        .anyRequest().authenticated()
                )

                .cors()
                .and()

                .csrf(csrf -> csrf.disable())

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customSuccessHandler)
                );

        return http.build();
    }
}

