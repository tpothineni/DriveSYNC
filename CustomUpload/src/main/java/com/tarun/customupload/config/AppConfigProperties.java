package com.tarun.customupload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *Configuration properties class defined in application.properties
 */
@ConfigurationProperties(prefix = "app.frontend")
public class AppConfigProperties {

    private String redirectUrl;

    private String logoutUrl;

    private String webpageUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getWebpageUrl() {
        return webpageUrl;
    }

    public void setWebpageUrl(String webpageUrl) {
        this.webpageUrl = webpageUrl;
    }
}
