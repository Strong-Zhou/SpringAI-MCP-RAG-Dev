package com.zhou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    // http://localhost:8090/hello
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}