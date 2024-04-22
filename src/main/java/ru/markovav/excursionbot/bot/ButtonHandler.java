package ru.markovav.excursionbot.bot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@FunctionalInterface
public interface ButtonHandler {
    void handle(CallbackQuery query, String data);
}
