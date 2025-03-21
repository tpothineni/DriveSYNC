package com.tarun.customupload;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomSuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Optionally, extract user details from the 'authentication' object if needed.
        // For example, you might include a token or user information in the redirect URL as query params.

        // Redirect to your React app URL.
        String redirectUrl = "http://localhost:3000";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
