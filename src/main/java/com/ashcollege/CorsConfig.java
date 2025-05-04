package com.ashcollege;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // שמי שיהיה ה-frontend שלך
                .allowedOrigins(
                        "http://localhost:19006",
                        "https://math-journey-front.onrender.com"
                )
                // שיטות HTTP מותרות
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // כותרות שה-browser ישלח (כאן חשוב Authorization ו-Content-Type)
                .allowedHeaders("Authorization", "Content-Type")
                // כותרות שה-browser יחשוף לצד הלקוח (אם אתה צריך לבדוק אותן ב-JS)
                .exposedHeaders("Authorization")
                // אם אתה עובד עם cookies או credentials
                .allowCredentials(true);
    }
}
