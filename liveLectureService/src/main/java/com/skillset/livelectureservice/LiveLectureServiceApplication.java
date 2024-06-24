package com.skillset.livelectureservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LiveLectureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveLectureServiceApplication.class, args);
    }

}
