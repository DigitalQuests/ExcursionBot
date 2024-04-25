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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.markovav.excursionbot.Base64Utils.uuidToB64;

@Service
@Log4j2
public class BotService implements LongPollingSingleThreadUpdateConsumer {
    public static final String CALLBACK_SEPARATOR = ":";
    private final TelegramBotsLongPollingApplication botsApplication =
            new TelegramBotsLongPollingApplication();
    @Getter
    private final TelegramClient telegramClient;
    @Getter
    private final String botUsername;
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private final Map<String, ButtonHandler> buttonHandlers = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @SneakyThrows
    public BotService(@Value("${bot.token}") String token) {
        telegramClient = new OkHttpTelegramClient(token);
        var me = telegramClient.execute(GetMe.builder().build());
        botUsername = me.getUserName();
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
                if (message.getWebAppData() != null) {
                    var data = message.getWebAppData().getData();
                    log.info("User {} scanned QR with data {}", message.getFrom().getUserName(), data);
                    if (data.startsWith("{")) {
                        commandHandlers.get("createRoute").handle(message, new String[]{data});
                    }
                    else {
                        commandHandlers.get("qr").handle(message, new String[]{data});
                    }
                }
                return;
            }
            var command = message.getText().toLowerCase().split(" ")[0];
            if (!command.startsWith("/")) {
                return;
            }
            command = command.substring(1);
            log.info(
                    "User {} sent command: {} (message: {})",
                    message.getFrom().getUserName(),
                    command,
                    message.getText());
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

            log.info(
                    "User {} pressed button: {} (query: {})",
                    callbackQuery.getFrom().getUserName(),
                    button,
                    data);

            var handler = buttonHandlers.get(button);
            if (handler == null) {
                log.error("No handler for button: {}", button);
                return;
            }
            handler.handle(callbackQuery, parts[1]);
        }
    }

    public void sendBatch(SendMessage... sendMessages) {
        executorService.submit(() -> sendBatchJob(sendMessages));
    }

    @SneakyThrows
    private void sendBatchJob(SendMessage... sendMessages) {
        for (var msg : sendMessages) {
            try {
                telegramClient.execute(msg);
            } catch (TelegramApiException e) {
                log.error("Failed to send message", e);
                if (e instanceof TelegramApiRequestException && ((TelegramApiRequestException) e).getErrorCode() == 429) {
                    log.warn("Rate limited, waiting for 3 second");
                    Thread.sleep(3000);
                    telegramClient.execute(msg);
                } else {
                    log.error("Failed to send message", e);
                }
            }
            Thread.sleep(250);
        }
    }

    public String createCallbackData(String method, Object... parts) {
        List<String> transformedParts = new ArrayList<>();

        for (Object part : parts) {
            if (part instanceof UUID uuid) {
                // UUIDs are encoded as Base64
                transformedParts.add(uuidToB64(uuid));
                continue;
            }
            transformedParts.add(part.toString());
        }

        var result = method + CALLBACK_SEPARATOR + String.join(CALLBACK_SEPARATOR, transformedParts);
        if (result.getBytes(StandardCharsets.UTF_8).length > 64) {
            throw new IllegalArgumentException("Callback data is too long: " + result);
        }

        return result;
    }
}
