package ru.markovav.excursionbot.bot.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.util.UUID;

@Component
public class QR {
    private final UserService userService;
    private final ExcursionService excursionService;

    public QR(
            BotService botService,
            UserService userService,
            ExcursionService excursionService) {
        botService.registerCommandHandler("qr", this::handle);
        this.userService = userService;
        this.excursionService = excursionService;
    }

    private void handle(Message message, String[] strings) {
        var user = userService.ensureCreated(message.getFrom());
        excursionService.consumeQR(UUID.fromString(strings[0]), user);
    }
}
