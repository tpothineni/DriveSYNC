package com.tarun.customupload.handler;

import com.tarun.customupload.config.AppConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

class CustomSuccessHandlerTest {

    @Test
    void testRedirectsToConfiguredUrl() throws Exception {
        AppConfigProperties config = new AppConfigProperties();
        config.setRedirectUrl("http://localhost:3000");

        CustomSuccessHandler handler = new CustomSuccessHandler(config);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = null;

        handler.onAuthenticationSuccess(request, response, auth);
        assertEquals("http://localhost:3000", response.getRedirectedUrl());
    }
}
