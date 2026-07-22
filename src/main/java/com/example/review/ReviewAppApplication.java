package com.example.review;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ReviewAppApplication {

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(
                TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        );

        System.out.println(
                "Current timezone: " + TimeZone.getDefault().getID()
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(ReviewAppApplication.class, args);
    }
}