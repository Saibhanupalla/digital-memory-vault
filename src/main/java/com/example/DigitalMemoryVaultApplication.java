package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableScheduling
public class DigitalMemoryVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalMemoryVaultApplication.class, args);
    }

    @GetMapping
    public String helloWorld(){
        return "Hello, World";
    }
}
