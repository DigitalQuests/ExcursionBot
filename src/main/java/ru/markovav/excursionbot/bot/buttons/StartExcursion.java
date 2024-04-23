package ru.markovav.excursionbot.bot.buttons;

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
import ru.markovav.excursionbot.repositories.ExcursionRepository;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.services.ExcursionService;
import ru.markovav.excursionbot.services.UserService;

import java.time.Instant;
import java.util.UUID;

@Component
public class StartExcursion {
    private final BotService botService;
    private final UserService userService;
    private final RouteRepository routeRepository;
    private final ExcursionService excursionService;
    private final ExcursionRepository excursionRepository;

    public StartExcursion(BotService botService, UserService userService, RouteRepository routeRepository, ExcursionService excursionService, ExcursionRepository excursionRepository) {
        this.botService = botService;
        botService.registerButtonHandler("startExcursion", this::handle);
        this.userService = userService;
        this.routeRepository = routeRepository;
        this.excursionService = excursionService;
        this.excursionRepository = excursionRepository;
    }

    @SneakyThrows
    public void handle(CallbackQuery query, String data) {
        var excursionOpt = excursionRepository.findById(UUID.fromString(data));
        if (excursionOpt.isEmpty()) {
            botService.getTelegramClient().execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(query.getId())
                    .text("Экскурсия не найдена").build());
            return;
        }

        var excursion = excursionOpt.get();
        excursion.setStartedAt(Instant.now());
        excursionRepository.save(excursion);


    }
}
