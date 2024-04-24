package ru.markovav.excursionbot.bot.buttons;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.bot.UuidHandlerAdapter;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.util.UUID;

@Component
public class Hint {
  private final UserService userService;
  private final ExcursionService excursionService;

  public Hint(
      BotService botService,
      UserService userService,
      ExcursionService excursionService
  ) {
    this.userService = userService;
    this.excursionService = excursionService;
    botService.registerButtonHandler("hint", new UuidHandlerAdapter(this::handleAnswer));
  }

  private void handleAnswer(CallbackQuery query, UUID... uuids) {
    var user = userService.ensureCreated(query.getFrom());

    var excursionTaskId = uuids[0];

    excursionService.sendHint(query, user, excursionTaskId);
  }
}
