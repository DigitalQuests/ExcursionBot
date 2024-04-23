package ru.markovav.excursionbot.bot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.markovav.excursionbot.Base64Utils;

import java.util.UUID;

public class UuidHandlerAdapter implements ButtonHandler {

  private final UuidDataButtonHandler handler;

  public UuidHandlerAdapter(UuidDataButtonHandler handler) {
    this.handler = handler;
  }

  @Override
  public void handle(CallbackQuery query, String data) {
    var parts = data.split(BotService.CALLBACK_SEPARATOR);
    var uuids = new UUID[parts.length];
    for (int i = 0; i < parts.length; i++) {
      uuids[i] = Base64Utils.b64ToUuid(parts[i]);
    }
    handler.handle(query, uuids);
  }

  @FunctionalInterface
  public interface UuidDataButtonHandler {
    void handle(CallbackQuery query, UUID... uuids);
  }
}
