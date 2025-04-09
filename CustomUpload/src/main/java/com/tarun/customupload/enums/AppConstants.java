package com.tarun.customupload.enums;

public enum AppConstants {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    JSESSIONID("JSESSIONID"),
    XSRF_TOKEN("XSRF-TOKEN"),
    DriveSyncApp("DriveSyncApp");


    private final String value;

    AppConstants(String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }
}
