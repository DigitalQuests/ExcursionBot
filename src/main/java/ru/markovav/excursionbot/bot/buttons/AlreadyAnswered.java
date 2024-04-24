package ru.markovav.excursionbot.bot.buttons;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.bot.UuidHandlerAdapter;

import java.util.UUID;

@Component
public class AlreadyAnswered {
  private final BotService botService;

  public AlreadyAnswered(BotService botService) {
    this.botService = botService;
    botService.registerButtonHandler("alreadyAnswered", new UuidHandlerAdapter(this::handleAlreadyAnswer));
  }

  @SneakyThrows
  private void handleAlreadyAnswer(CallbackQuery query, UUID... uuids) {
    botService.getTelegramClient().execute(
        AnswerCallbackQuery.builder()
            .callbackQueryId(query.getId())
            .text("Вы уже выбирали этот вариант ответа")
            .build());
  }
}