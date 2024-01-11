package com.csrd.pims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //启用定时任务支持.
@EnableAsync //异步支持
@SpringBootApplication
public class PimsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PimsServiceApplication.class, args);
    }

}
