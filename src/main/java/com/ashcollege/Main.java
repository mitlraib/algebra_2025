package com.ashcollege;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ashcollege.service.Persist;

@SpringBootApplication
@EnableScheduling

public class Main {


    public static boolean applicationStarted = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(Persist.class);

    public static long startTime;


    public static void main(String[] args) {
        System.out.println("ENV DATABASE_URL = " + System.getenv("SPRING_DATASOURCE_URL"));
        System.out.println("ENV DB_USER      = " + System.getenv("SPRING_DATASOURCE_USERNAME"));
        System.out.println("ENV DB_PASS      = " + System.getenv("SPRING_DATASOURCE_PASSWORD"));
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        LOGGER.info("Application started.");
        applicationStarted = true;
        startTime = System.currentTimeMillis();

    }

}
