package com.example.SPExtractorBackend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Enable CORS for frontend
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply to all endpoints under "/api/**"
                .allowedOrigins("http://127.0.0.1:5500", "https://localhost:8443") // Frontend origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Methods to allow
                .allowedHeaders("Authorization", "Content-Type", "Accept") // Headers to allow
                .allowCredentials(true); // Allow cookies if needed
    }
}
