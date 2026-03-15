package com.example.taskcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskCenterApplication.class, args);
    }
}
