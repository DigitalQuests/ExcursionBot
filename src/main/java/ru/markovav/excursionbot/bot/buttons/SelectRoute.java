package ru.markovav.excursionbot.bot.buttons;

import java.util.UUID;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.bot.UuidHandlerAdapter;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

@Component
public class SelectRoute {
  private final BotService botService;
  private final UserService userService;
  private final RouteRepository routeRepository;
  private final ExcursionService excursionService;

  public SelectRoute(
      BotService botService,
      UserService userService,
      RouteRepository routeRepository,
      ExcursionService excursionService) {
    this.botService = botService;
    botService.registerButtonHandler("selectRoute", new UuidHandlerAdapter(this::handle));
    this.userService = userService;
    this.routeRepository = routeRepository;
    this.excursionService = excursionService;
  }

  @SneakyThrows
  public void handle(CallbackQuery query, UUID... uuids) {
    var route = routeRepository.findById(uuids[0]);
    var user = userService.ensureCreated(query.getFrom());

    if (route.isEmpty()) {
      botService
          .getTelegramClient()
          .execute(
              AnswerCallbackQuery.builder()
                  .callbackQueryId(query.getId())
                  .text("Маршрут не найден")
                  .build());
      return;
    }

    var excursion = excursionService.startExcursion(route.get(), user);

    botService
        .getTelegramClient()
        .execute(AnswerCallbackQuery.builder().callbackQueryId(query.getId()).build());

    botService
        .getTelegramClient()
        .execute(
            SendPhoto.builder()
                .chatId(query.getFrom().getId())
                .photo(new InputFile(excursionService.getExcursionQR(excursion), "qr.png"))
                .caption("Покажите этот QR вашим слушателям, чтобы они присоединились к экскурсии.")
                .replyMarkup(
                    InlineKeyboardMarkup.builder()
                        .keyboardRow(
                            new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                    .text("Начать экскурсию")
                                    .callbackData(
                                        botService.createCallbackData(
                                            "startExcursion", excursion.getId()))
                                    .build(),
                                InlineKeyboardButton.builder()
                                    .text("Отменить")
                                    .callbackData(
                                        botService.createCallbackData(
                                            "cancelExcursion", excursion.getId()))
                                    .build()))
                        .build())
                .build());
  }
}
