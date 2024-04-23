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
import ru.markovav.excursionbot.repositories.ExcursionRepository;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.util.UUID;
import java.util.stream.StreamSupport;

@Component
public class Start {
    private final BotService botService;
    private final UserService userService;
    private final RouteRepository routeRepository;
    private final ExcursionRepository excursionRepository;
    private final ExcursionService excursionService;

    public Start(BotService botService, UserService userService, RouteRepository routeRepository, ExcursionRepository excursionRepository, ExcursionService excursionService) {
        this.botService = botService;
        botService.registerCommandHandler("start", this::handle);
        this.userService = userService;
        this.routeRepository = routeRepository;
        this.excursionRepository = excursionRepository;
        this.excursionService = excursionService;
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
            var excursionId = UUID.fromString(args[0]);
            var excursion = excursionRepository.findById(excursionId);

            if (excursion.isEmpty()) {
                botService.getTelegramClient().execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text("Экскурсия не найдена")
                        .build());
                return;
            }

            excursionService.joinExcursion(excursion.get(), user);

            botService.getTelegramClient().execute(SendMessage.builder()
                    .chatId(message.getChatId().toString())
                    .text("Вы присоединились к экскурсии!")
                    .build());

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
