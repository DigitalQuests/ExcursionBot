package ru.markovav.excursionbot;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import ru.markovav.excursionbot.seeders.RouteSeeder;

@SpringBootApplication
@RequiredArgsConstructor
@EnableJpaAuditing
public class ExcursionBotApplication implements CommandLineRunner {
  private final RouteSeeder routeSeeder;

  public static void main(String[] args) {
    SpringApplication.run(ExcursionBotApplication.class, args);
  }

  @Override
  public void run(String... args) {
    var argList = Arrays.asList(args);

    if (argList.contains("purge")) {
      routeSeeder.purge();
    }
    if (argList.contains("seed")) {
      routeSeeder.seed();
    }

    if (!argList.isEmpty()) {
      System.exit(0);
    }
  }
}
