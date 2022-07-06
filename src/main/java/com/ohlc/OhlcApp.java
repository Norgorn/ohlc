package com.ohlc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.invoke.MethodHandles;

@SpringBootApplication
@EnableScheduling
public class OhlcApp {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        try {
            SpringApplication.run(OhlcApp.class, args);
        } catch (Exception e) {
            logger.error("Exiting because of error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
