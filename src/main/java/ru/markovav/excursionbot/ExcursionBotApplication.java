package ru.markovav.excursionbot;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.markovav.excursionbot.seeders.ExcursionSeeder;

import java.util.Objects;

@SpringBootApplication
@RequiredArgsConstructor
public class ExcursionBotApplication implements CommandLineRunner {
    private final ExcursionSeeder excursionSeeder;

    public static void main(String[] args) {
        SpringApplication.run(ExcursionBotApplication.class, args);
    }

    @Override
    public void run(String... args) {
        var zeroArg = args.length == 0 ? null : args[0];
        if (Objects.equals(zeroArg, "seed")) {
            excursionSeeder.seed();
            System.exit(0);
        }
    }
}
