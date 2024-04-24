package ru.markovav.excursionbot.bot.buttons;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.bot.UuidHandlerAdapter;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.util.UUID;

@Component
public class Answer {
  private final UserService userService;
  private final ExcursionService excursionService;

  public Answer(
      BotService botService,
      UserService userService,
      ExcursionService excursionService
  ) {
    this.userService = userService;
    this.excursionService = excursionService;
    botService.registerButtonHandler("answer", new UuidHandlerAdapter(this::handleAnswer));
  }

  private void handleAnswer(CallbackQuery query, UUID... uuids) {
    var user = userService.ensureCreated(query.getFrom());

    var answerVariantId = uuids[0];
    var excursionTaskId = uuids[1];

    excursionService.acceptAnswer(query, user, answerVariantId, excursionTaskId);
  }
}
