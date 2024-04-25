package ru.markovav.excursionbot.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.*;
import ru.markovav.excursionbot.repositories.ExcursionTaskRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

  private final BotService botService;
  private final ExcursionTaskRepository excursionTaskRepository;

  @SneakyThrows
  public void sendTaskToUser(ExcursionTask et, User user) {
    var keyboardRows = constructAnswersKeyboard(et);

    var sendMessage = SendMessage.builder()
        .chatId(user.getTelegramId())
        .text(et.getTask().getText())
        .replyMarkup(keyboardRows)
        .build();

    botService.getTelegramClient().execute(sendMessage);
  }

  public @NotNull InlineKeyboardMarkup constructAnswersKeyboard(ExcursionTask et) {
    var keyboardRows =
        et.getTask().getAnswerVariants().stream()
            .map(av -> answerVariantToKeyboard(et, av))
            .collect(Collectors.toList());

    helpButtonRow(et, keyboardRows);

    return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
  }

  public void helpButtonRow(ExcursionTask et, List<InlineKeyboardRow> keyboardRows) {
    if (et.getUsedHints() >= et.getTask().getHints().size()) {
      return;
    }

    var button =
        InlineKeyboardButton.builder()
            .text("Подсказка (минус балл)")
            /* Help:[excursion_task_id] */
            .callbackData(botService.createCallbackData("hint", et.getId()))
            .build();

    keyboardRows.add(new InlineKeyboardRow(List.of(button)));
  }

  public InlineKeyboardRow answerVariantToKeyboard(
      ExcursionTask et, AnswerVariant answerVariant) {
    var isUsed = et.getUsedAnswers().stream().anyMatch(av -> av.getId().equals(answerVariant.getId()));
    var button =
        InlineKeyboardButton.builder()
            .text((isUsed ? "❌ " : "") + answerVariant.getText())
            /* Answer:[answer_variant_id]:[excursion_task_id] */
            .callbackData(isUsed
                ? botService.createCallbackData("alreadyAnswered", answerVariant.getId())
                : botService.createCallbackData("answer", answerVariant.getId(), et.getId()))
            .build();
    return new InlineKeyboardRow(List.of(button));
  }

  public ExcursionTask assignTaskToUser(Task task, Excursion excursion, User user) {
    var excursionTask = ExcursionTask.builder()
        .mistakes(0)
        .usedHints(0)
        .task(task)
        .excursion(excursion)
        .participant(user)
        .usedAnswers(Set.of())
        .build();

    return excursionTaskRepository.save(excursionTask);
  }
}
