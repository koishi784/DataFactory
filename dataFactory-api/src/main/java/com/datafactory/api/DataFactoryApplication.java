package com.datafactory.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.datafactory")
public class DataFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataFactoryApplication.class, args);
    }

}
