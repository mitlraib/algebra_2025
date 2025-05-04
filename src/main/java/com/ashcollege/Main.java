package com.ashcollege;

import com.ashcollege.service.Persist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class Main {


    public static boolean applicationStarted = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(Persist.class);

    public static long startTime;


    public static void main(String[] args) {
        System.out.println("ENV DATABASE_URL = " + System.getenv("DATABASE_URL"));
        System.out.println("ENV DB_USER      = " + System.getenv("DB_USER"));
        System.out.println("ENV DB_PASS      = " + System.getenv("DB_PASS"));
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        LOGGER.info("Application started.");
        applicationStarted = true;
        startTime = System.currentTimeMillis();

    }

}
