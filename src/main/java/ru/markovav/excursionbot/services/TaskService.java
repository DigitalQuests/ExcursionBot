package ru.markovav.excursionbot.services;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.*;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final BotService botService;

  @SneakyThrows
  public void sendTaskToUser(ExcursionTask et, User user) {
    var keyboardRows =
        et.getTask().getAnswerVariants().stream()
            .map(av -> answerVariantToKeyboard(et, av))
            .collect(Collectors.toList());

    keyboardRows.add(helpButtonRow(et));

    var sendMessage = SendMessage.builder()
        .chatId(user.getTelegramId())
        .text(et.getTask().getText())
        .replyMarkup(new InlineKeyboardMarkup(keyboardRows))
        .build();

    botService.getTelegramClient().execute(sendMessage);
  }

  private InlineKeyboardRow helpButtonRow(ExcursionTask et) {
    var button =
        InlineKeyboardButton.builder()
            .text("Подсказка (-очко)")
            /* Help:[excursion_task_id] */
            .callbackData(botService.createCallbackData("help", et.getId()))
            .build();
    return new InlineKeyboardRow(List.of(button));
  }

  private InlineKeyboardRow answerVariantToKeyboard(
      ExcursionTask et, AnswerVariant answerVariant) {
    var button =
        InlineKeyboardButton.builder()
            .text(answerVariant.getText())
            /* Answer:[answer_variant_id]:[excursion_task_id] */
            .callbackData(botService.createCallbackData("answer", answerVariant.getId(), et.getId()))
            .build();
    return new InlineKeyboardRow(List.of(button));
  }

}
