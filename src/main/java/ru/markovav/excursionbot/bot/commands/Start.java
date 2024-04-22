package ru.markovav.excursionbot.bot.commands;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.Role;
import ru.markovav.excursionbot.models.Route;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.services.UserService;

import java.util.stream.StreamSupport;

@Component
public class Start {
    private final BotService botService;
    private final UserService userService;
    private final RouteRepository routeRepository;

    public Start(BotService botService, UserService userService, RouteRepository routeRepository) {
        this.botService = botService;
        botService.registerCommandHandler("start", this::handle);
        this.userService = userService;
        this.routeRepository = routeRepository;
    }

    @SneakyThrows
    public void handle(Message message, String[] args) {
        var user = userService.ensureCreated(message.getFrom());

        if (user.getRole() == Role.PARTICIPANT) {
            if (args.length == 0) {
                botService.getTelegramClient().execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text("Добро пожаловать в бота для экскурсий! Чтобы начать экскурсию, попросите QR код у вашего экскурсовода.")
                        .build());
            }
            // TODO: get excursion by qr code
            return;
        }

        var routes = routeRepository.findAll();

        var markup = InlineKeyboardMarkup.builder().keyboard(
                StreamSupport.stream(routes.spliterator(), false)
                        .map(this::routeToButton)
                        .map(InlineKeyboardRow::new)
                        .toList()
        ).build();

        botService.getTelegramClient().execute(SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Выберите маршрут:")
                .replyMarkup(markup)
                .build());
    }

    private InlineKeyboardButton routeToButton(Route route) {
        return InlineKeyboardButton.builder()
                .text(route.getName())
                .callbackData("selectRoute" + BotService.CALLBACK_SEPARATOR + route.getId())
                .build();
    }
}
