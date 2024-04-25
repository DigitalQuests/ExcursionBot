package ru.markovav.excursionbot.bot.commands;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.Role;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.util.List;

@Component
public class Dev {
  private @Value("${webapp-url}") String webappUrl;
  private final BotService botService;
  private final UserService userService;
  private final RouteRepository routeRepository;
  private final ExcursionService excursionService;

  public Dev(BotService botService, UserService userService, RouteRepository routeRepository, ExcursionService excursionService) {
    this.botService = botService;
    botService.registerCommandHandler("dev", this::handle);
    this.userService = userService;
    this.routeRepository = routeRepository;
    this.excursionService = excursionService;
  }

  @SneakyThrows
  public void handle(Message message, String[] args) {
    var user = userService.ensureCreated(message.getFrom());

    if (user.getRole() != Role.GUIDE) {
      return;
    }

    botService
        .getTelegramClient().execute(
            SendMessage.builder()
                .chatId(message
                    .getChatId()
                    .toString()
                )
                .text("Конструктор экскурсий")
                .replyMarkup(ReplyKeyboardMarkup.builder()
                    .keyboardRow(
                        new KeyboardRow(List.of(
                            KeyboardButton.builder()
                                .text("Создать экскурсию")
                                .webApp(WebAppInfo
                                    .builder()
                                    .url(webappUrl)
                                    .build()
                                ).build()
                        ))).build()
                ).build()
        );
  }
}
