package ru.markovav.excursionbot.bot;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class BotService implements LongPollingSingleThreadUpdateConsumer {
    public static final String CALLBACK_SEPARATOR = ":";
    private final TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
    @Getter private final TelegramClient telegramClient;
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private final Map<String, ButtonHandler> buttonHandlers = new HashMap<>();

    @SneakyThrows
    public BotService(@Value("${bot.token}") String token) {
        telegramClient = new OkHttpTelegramClient(token);
        var me = telegramClient.execute(GetMe.builder().build());
        System.out.println(me);
        botsApplication.registerBot(token, this);
    }

    @PreDestroy
    @SneakyThrows
    public void destroy() {
        botsApplication.stop();
    }

    public void registerCommandHandler(String command, CommandHandler handler) {
        commandHandlers.put(command, handler);
    }

    public void registerButtonHandler(String button, ButtonHandler handler) {
        buttonHandlers.put(button, handler);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();
            if (!message.hasText()) {
                return;
            }
            var command = message.getText().toLowerCase().split(" ")[0];
            if (!command.startsWith("/")) {
                return;
            }
            command = command.substring(1);
            log.info("User {} sent command: {} (message: {})", message.getFrom().getUserName(), command, message.getText());
            var handler = commandHandlers.get(command);
            if (handler == null) {
                log.warn("No handler for command: {}", command);
                return;
            }
            var split = message.getText().split(" ", 2);
            if (split.length == 1) {
                handler.handle(message, new String[0]);
                return;
            }
            handler.handle(message, split[1].split(" "));
        } else if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            var data = callbackQuery.getData();

            var parts = data.split(CALLBACK_SEPARATOR, 2);

            var button = parts[0];

            log.info("User {} pressed button: {} (query: {})", callbackQuery.getFrom().getUserName(), button, data);

            var handler = buttonHandlers.get(button);
            if (handler == null) {
                log.error("No handler for button: {}", button);
                return;
            }
            handler.handle(callbackQuery, parts[1]);
        }
    }
}
