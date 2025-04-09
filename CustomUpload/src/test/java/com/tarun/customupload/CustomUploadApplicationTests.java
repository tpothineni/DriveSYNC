package com.tarun.customupload;

import com.tarun.customupload.service.GoogleDriveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CustomUploadApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void testServiceBeanLoads() {
        assertNotNull(context.getBean(GoogleDriveService.class));
    }
}
