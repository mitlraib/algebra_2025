package com.ashcollege;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://math-journey-front.onrender.com", // הפרונט בענן
                        "http://localhost:8081"                    // פיתוח מקומי
                )
                .allowedMethods("*")
                .allowCredentials(true);
    }

}
