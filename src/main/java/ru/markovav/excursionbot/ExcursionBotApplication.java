package ru.markovav.excursionbot;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import ru.markovav.excursionbot.seeders.ExcursionSeeder;

@SpringBootApplication
@RequiredArgsConstructor
@EnableJpaAuditing
public class ExcursionBotApplication implements CommandLineRunner {
  private final ExcursionSeeder excursionSeeder;

  public static void main(String[] args) {
    SpringApplication.run(ExcursionBotApplication.class, args);
  }

  @Override
  public void run(String... args) {
    var argList = Arrays.asList(args);

    if (argList.contains("purge")) {
      excursionSeeder.purge();
      System.exit(0);
    }
    if (argList.contains("seed")) {
      excursionSeeder.seed();
      System.exit(0);
    }
  }
}
