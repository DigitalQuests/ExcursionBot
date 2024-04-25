package ru.markovav.excursionbot.bot.buttons;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.bot.UuidHandlerAdapter;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.util.UUID;

@Component
public class NextTask {
  private final BotService botService;
  private final UserService userService;
  private final ExcursionService excursionService;

  public NextTask(
      BotService botService,
      UserService userService,
      ExcursionService excursionService
  ) {
    this.botService = botService;
    this.userService = userService;
    this.excursionService = excursionService;
    botService.registerButtonHandler("nextTask", new UuidHandlerAdapter(this::handleNextTask));
  }

  private void handleNextTask(CallbackQuery query, UUID... uuids) {
    var user = userService.ensureCreated(query.getFrom());

    excursionService.ifExists(
        uuids[0],
        excursion -> {
          excursionService.sendNextLocation(excursion, user);
          removeButtons(query);
        },
        () -> excursionNotFound(query));
  }

  @SneakyThrows
  private void excursionNotFound(CallbackQuery query) {
    botService
        .getTelegramClient()
        .execute(
            AnswerCallbackQuery.builder()
                .callbackQueryId(query.getId())
                .text("Экскурсия не найдена")
                .build());

    removeButtons(query);
  }

  @SneakyThrows
  private void removeButtons(CallbackQuery query) {
    var removeKeyboard =
        EditMessageReplyMarkup.builder()
            .chatId(query.getMessage().getChatId())
            .messageId(query.getMessage().getMessageId())
            .replyMarkup(InlineKeyboardMarkup.builder().build())
            .build();
    botService.getTelegramClient().execute(removeKeyboard);
  }
}
