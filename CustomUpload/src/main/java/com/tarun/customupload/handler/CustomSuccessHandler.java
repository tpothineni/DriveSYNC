package com.tarun.customupload.handler;

import com.tarun.customupload.config.AppConfigProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * It handles the OAuth Success events
 */
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppConfigProperties configProperties;

    public CustomSuccessHandler(AppConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String redirectUrl = configProperties.getRedirectUrl(); // Fetched from application.properties
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
