package ru.markovav.excursionbot.bot;

import org.telegram.telegrambots.meta.api.objects.message.Message;

@FunctionalInterface
public interface CommandHandler {
  void handle(Message message, String[] args);
}
