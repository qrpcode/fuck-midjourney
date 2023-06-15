package com.example.midjourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.example.midjourney.mapper")
public class MidjourneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MidjourneyApplication.class, args);
    }

}
