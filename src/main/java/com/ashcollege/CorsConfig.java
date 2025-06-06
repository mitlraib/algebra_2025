package com.ashcollege;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:8080",   // Expo‑web בדרך‑כלל
                        "http://localhost:8081",   // אם את על 8081
                        "http://localhost:19006",  // Expo‑Go
                        "https://math-journey-front.onrender.com"
                ).allowedMethods("GET","POST","PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type").exposedHeaders("Authorization")
                .allowCredentials(false);      // לא עובדים עם קוקיז – אפשר false

    }
}
