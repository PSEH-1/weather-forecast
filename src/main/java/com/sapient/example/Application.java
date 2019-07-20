package com.sapient.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/*
 * This is the main Spring Boot application class. It configures Spring Boot, JPA, Swagger
 */

@SpringBootApplication(scanBasePackages = {"com.sapient.example"})

public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
