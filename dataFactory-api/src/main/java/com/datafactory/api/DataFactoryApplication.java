package com.datafactory.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.datafactory")
@MapperScan("com.datafactory.core.domain.mapper")
public class DataFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataFactoryApplication.class, args);
    }

}
