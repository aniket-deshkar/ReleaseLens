package dev.releaselens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReleaseLensApplication {
    public static void main(String[] args) { SpringApplication.run(ReleaseLensApplication.class, args); }
}
