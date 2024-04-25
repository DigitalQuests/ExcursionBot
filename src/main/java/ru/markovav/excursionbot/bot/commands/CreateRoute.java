package ru.markovav.excursionbot.bot.commands;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.AnswerVariant;
import ru.markovav.excursionbot.models.Hint;
import ru.markovav.excursionbot.models.Route;
import ru.markovav.excursionbot.models.Task;
import ru.markovav.excursionbot.repositories.AnswerVariantRepository;
import ru.markovav.excursionbot.repositories.HintRepository;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.repositories.TaskRepository;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

@Component
public class CreateRoute {
  private static final Gson gson = new Gson();
  private final UserService userService;
  private final ExcursionService excursionService;
  private final AnswerVariantRepository answerVariantRepository;
  private final TaskRepository taskRepository;
  private final RouteRepository routeRepository;
  private final HintRepository hintRepository;
  private final BotService botService;

  public CreateRoute(
      BotService botService,
      UserService userService,
      ExcursionService excursionService,
      AnswerVariantRepository answerVariantRepository,
      TaskRepository taskRepository,
      RouteRepository routeRepository,
      HintRepository hintRepository) {
    botService.registerCommandHandler("createRoute", this::handle);
    this.userService = userService;
    this.excursionService = excursionService;
    this.answerVariantRepository = answerVariantRepository;
    this.taskRepository = taskRepository;
    this.routeRepository = routeRepository;
    this.hintRepository = hintRepository;
    this.botService = botService;
  }

  @SneakyThrows
  private void handle(Message message, String[] strings) {
    var route = gson.fromJson(strings[0], ru.markovav.excursionbot.models.CreateRoute.class);

    var routeDb = routeRepository.save(Route.builder()
        .name(route.getName())
        .welcomeMessage(route.getWelcomeMessage())
        .build());

    for (var i = 0; i < route.getTasks().size(); i++) {
      var task = route.getTasks().get(i);

      var taskDb = taskRepository.save(Task.builder()
          .index(i + 1)
          .route(routeDb)
          .name(task.getName())
          .location(task.getLocation())
          .text(task.getText())
          .build());

      for (var answerVariant : task.getAnswerVariants()) {
        answerVariantRepository.save(AnswerVariant.builder()
            .task(taskDb)
            .text(answerVariant.getText())
            .isCorrect(answerVariant.isCorrect())
            .build());
      }

      for (var j = 0; j < task.getHints().size(); j++) {
        var hint = task.getHints().get(j);

        hintRepository.save(Hint.builder()
            .index(j + 1)
            .task(taskDb)
            .text(hint.getText())
            .build());
      }

      botService.getTelegramClient().execute(
          SendPhoto.builder()
              .chatId(message.getFrom().getId())
              .caption("QR code for task " + taskDb.getName())
              .photo(new InputFile(excursionService.generateQR("EXCURSION_BOT=" + taskDb.getId()), "qr.png")).build());
    }
  }
}
