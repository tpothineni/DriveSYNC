package com.tarun.customupload;

import com.tarun.customupload.config.AppConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppConfigProperties.class)
public class CustomUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomUploadApplication.class, args);
    }
}
