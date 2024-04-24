package ru.markovav.excursionbot.bot.buttons;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.bot.UuidHandlerAdapter;
import ru.markovav.excursionbot.models.Excursion;
import ru.markovav.excursionbot.models.User;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

@Component
public class ExcursionButtons {
  private final BotService botService;
  private final UserService userService;
  private final ExcursionService excursionService;

  public ExcursionButtons(
      BotService botService,
      UserService userService,
      ExcursionService excursionService
      ) {
    this.botService = botService;
    this.userService = userService;
    this.excursionService = excursionService;
    botService.registerButtonHandler("startExcursion", new UuidHandlerAdapter(this::handleStart));
    botService.registerButtonHandler("endExcursion", new UuidHandlerAdapter(this::handleEnd));
    botService.registerButtonHandler("cancelExcursion", new UuidHandlerAdapter(this::handleCancel));
  }

  public void handleCancel(CallbackQuery query, UUID... uuids) {
    var user = userService.ensureCreated(query.getFrom());
    if (!userService.isGuide(user)) {
      return;
    }

    excursionService.ifExists(
        uuids[0],
        excursion -> {
          excursionService.cancelExcursion(excursion);
          removeButtons(query);
        },
        () -> excursionNotFound(query));
  }

  public void handleEnd(CallbackQuery query, UUID... uuids) {
    var user = userService.ensureCreated(query.getFrom());
    if (!userService.isGuide(user)) {
      return;
    }

    excursionService.ifExists(
        uuids[0],
        excursion -> {
          excursionService.endExcursion(excursion);
          removeButtonsAndDisplayResults(query.getMessage(), excursion);
        },
        () -> excursionNotFound(query));
  }

  @SneakyThrows
  public void handleStart(CallbackQuery query, UUID... uuids) {
    var user = userService.ensureCreated(query.getFrom());
    if (!userService.isGuide(user)) {
      return;
    }

    excursionService.ifExists(
        uuids[0],
        excursion -> {
          excursionService.startExcursion(excursion);
          setStartedButtons(query.getMessage(), excursion);
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
  private void setStartedButtons(MaybeInaccessibleMessage message, Excursion excursion) {
    var buttons =
        List.of(
            InlineKeyboardButton.builder()
                .text("Завершить экскурсию")
                .callbackData(botService.createCallbackData("endExcursion", excursion.getId()))
                .build(),
            InlineKeyboardButton.builder()
                .text("Обновить результаты")
                .callbackData(botService.createCallbackData("updateResults", excursion.getId()))
                .build());

    var rows = buttons.stream().map(InlineKeyboardRow::new).toList();
    var markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
    var method =
        EditMessageReplyMarkup.builder()
            .chatId(message.getChatId())
            .replyMarkup(markup)
            .messageId(message.getMessageId())
            .build();

    botService.getTelegramClient().execute(method);
  }

  @SneakyThrows
  private void removeButtonsAndDisplayResults(
      MaybeInaccessibleMessage message, Excursion excursion) {
    var results = excursionService.getAllResults(excursion);
    var resultsMessage = "Результаты экскурсии (решено заданий, кол-во ошибок, кол-во подсказок):\n\n" +
        results.stream().map(User.ResultsStorage::toString).collect(Collectors.joining("\n"));

    EditMessageCaption resultsMessageAction =
        EditMessageCaption.builder()
            .chatId(message.getChatId())
            .messageId(message.getMessageId())
            .caption(resultsMessage)
            .parseMode(ParseMode.HTML)
            .build();

    botService.getTelegramClient().execute(resultsMessageAction);
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
