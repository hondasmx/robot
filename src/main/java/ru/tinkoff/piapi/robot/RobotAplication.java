package ru.tinkoff.piapi.robot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "ru.tinkoff.piapi.robot")
public class RobotAplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotAplication.class, args);
    }
}
