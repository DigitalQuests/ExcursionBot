package ru.markovav.excursionbot.seeders;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.markovav.excursionbot.models.AnswerVariant;
import ru.markovav.excursionbot.models.Hint;
import ru.markovav.excursionbot.models.Route;
import ru.markovav.excursionbot.models.Task;
import ru.markovav.excursionbot.repositories.*;

@Component
@RequiredArgsConstructor
@Log4j2
public class RouteSeeder {
  private final RouteRepository routeRepository;
  private final HintRepository hintRepository;
  private final TaskRepository taskRepository;
  private final AnswerVariantRepository answerVariantRepository;
  private final ExcursionRepository excursionRepository;
  private final ExcursionTaskRepository excursionTaskRepository;

  public void purge() {
    log.info("Purging database");
    excursionTaskRepository.deleteAll();
    excursionRepository.deleteAll();
    answerVariantRepository.deleteAll();
    hintRepository.deleteAll();
    taskRepository.deleteAll();
    routeRepository.deleteAll();
  }

  public void seed() {
    log.info("Seeding database");
    makeRoute();
  }

  private void makeHints(Task task) {
    var hint1 = Hint.builder().text("Hint 1").index(1).task(task).build();
    var hint2 = Hint.builder().text("Hint 2").index(2).task(task).build();

    hintRepository.save(hint1);
    hintRepository.save(hint2);
  }

  private void makeTasks(Route route) {
    var task1 = Task.builder().name("Task 1").text("Description 1").index(1).route(route).build();

    var task2 = Task.builder().name("Task 2").text("Description 2").index(2).route(route).build();

    taskRepository.save(task1);
    taskRepository.save(task2);

    makeAnswerVariants(task1, task2);
    makeHints(task2);
  }

  private void makeAnswerVariants(Task... tasks) {
    for (var task : tasks) {
      var answerVariant1 = AnswerVariant.builder().text("Correct 1").isCorrect(true).task(task).build();
      var answerVariant2 = AnswerVariant.builder().text("Incorrect 1").isCorrect(false).task(task).build();
      var answerVariant3 = AnswerVariant.builder().text("Incorrect 2").isCorrect(false).task(task).build();
      var answerVariant4 = AnswerVariant.builder().text("Correct 2").isCorrect(true).task(task).build();

      answerVariantRepository.save(answerVariant1);
      answerVariantRepository.save(answerVariant2);
      answerVariantRepository.save(answerVariant3);
      answerVariantRepository.save(answerVariant4);
    }
  }

  private void makeRoute() {
    var route = Route.builder().name("Route 1").welcomeMessage("Welcome message").build();

    routeRepository.save(route);

    makeTasks(route);
  }
}
