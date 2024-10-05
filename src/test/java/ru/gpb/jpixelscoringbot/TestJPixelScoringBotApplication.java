package ru.gpb.jpixelscoringbot;

import org.springframework.boot.SpringApplication;

public class TestJPixelScoringBotApplication {

    public static void main(String[] args) {
        SpringApplication.from(JPixelScoringBotApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
