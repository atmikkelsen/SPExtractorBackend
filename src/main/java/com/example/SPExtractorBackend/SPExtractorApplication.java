package com.example.SPExtractorBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SPExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SPExtractorApplication.class, args);
    }

}
