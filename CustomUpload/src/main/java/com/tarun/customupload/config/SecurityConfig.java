package com.tarun.customupload.config;

import com.tarun.customupload.enums.AppConstants;
import com.tarun.customupload.handler.CustomSuccessHandler;
import com.tarun.customupload.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 *Security configuration class for the application.
 *
 *- Enables OAuth2.0 Authorization and uses Custom syccess handler
 *- CORS Enabled by only allowing requests from frontend
 *- CSRF Enabled using cookie based token
 *- Configures logout behavior
 */
@Configuration
public class SecurityConfig {

    private final AppConfigProperties configProperties;

    public SecurityConfig(AppConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomSuccessHandler customSuccessHandler,
                                                   RestAuthenticationEntryPoint restEntryPoint) throws Exception {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/public").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restEntryPoint)
                )
                //Allowing CORS from frontend
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(configProperties.getWebpageUrl())); // e.g. http://localhost:3000
                    config.setAllowedMethods(List.of(AppConstants.GET.get(), AppConstants.POST.get(), AppConstants.PUT.get(), AppConstants.DELETE.get(), AppConstants.OPTIONS.get() ));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))
                //CSRF Enabled using cookie based token
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(handler) //Prevent automatic CSRF token rotation
                )
                //Enable Google OAuth2 login and use custom success handler
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customSuccessHandler)
                )

                //Customize logout behavior
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(configProperties.getLogoutUrl())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(AppConstants.JSESSIONID.get(), AppConstants.XSRF_TOKEN.get())
                );
        return http.build();
    }
}
