package ru.markovav.excursionbot.services;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.SneakyThrows;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.Excursion;
import ru.markovav.excursionbot.models.ExcursionTask;
import ru.markovav.excursionbot.models.Route;
import ru.markovav.excursionbot.models.User;
import ru.markovav.excursionbot.repositories.ExcursionRepository;
import ru.markovav.excursionbot.repositories.ExcursionTaskRepository;
import ru.markovav.excursionbot.repositories.TaskRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Transactional
public class ExcursionService {
  private final ExcursionRepository excursionRepository;
  private final BotService botService;
  private final ExcursionTaskRepository excursionTaskRepository;
  private final TaskRepository taskRepository;
  private final TaskService taskService;

  @SneakyThrows
  public ExcursionService(ExcursionRepository excursionRepository, BotService botService, ExcursionTaskRepository excursionTaskRepository, TaskRepository taskRepository, TaskService taskService) {
    this.excursionRepository = excursionRepository;
    this.botService = botService;
    this.excursionTaskRepository = excursionTaskRepository;
    this.taskRepository = taskRepository;
    this.taskService = taskService;
  }

  public Excursion startExcursion(Route route, User guide) {
    var excursion = Excursion.builder().route(route).guide(guide).createdAt(Instant.now()).build();

    excursionRepository.save(excursion);

    return excursion;
  }

  @SneakyThrows
  public void joinExcursion(Excursion excursion, User user) {
    excursion.getParticipants().add(user);
    excursionRepository.save(excursion);

    var sendMessage = SendMessage.builder()
        .chatId(user.getTelegramId())
        .text("Вы присоединились к экскурсии! Ожидайте начала.")
        .build();

    botService.getTelegramClient().execute(sendMessage);
  }

  public void startExcursion(Excursion excursion) {
    var startedAt = excursion.getStartedAt();
    if (startedAt != null && startedAt.isBefore(Instant.now())) {
      return;
    }

    var keyboard = InlineKeyboardMarkup.builder()
        .keyboardRow(
            new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                    .text("Начать!")
                    .callbackData(botService.createCallbackData("nextTask", excursion.getId()))
                    .build()
            )
        ).build();

    // broadcast message to all participants
    var sendMessages = excursion.getParticipants().stream()
        .map(User::getTelegramId)
        .map(tgId -> SendMessage.builder()
            .chatId(tgId.toString())
            .text(excursion.getRoute().getWelcomeMessage())
            .replyMarkup(keyboard)
            .build())
        .toArray(SendMessage[]::new);

    botService.sendBatch(sendMessages);

    excursion.setStartedAt(Instant.now());
    excursionRepository.save(excursion);
  }

  public void endExcursion(Excursion excursion) {
    excursion.setFinishedAt(Instant.now());
    excursionRepository.save(excursion);
  }

  public void cancelExcursion(Excursion excursion) {
    excursionRepository.delete(excursion);
  }

  @SneakyThrows
  public void nextTask(Excursion excursion, User user) {
    var nextIndex =
        excursionTaskRepository.findByExcursion_IdAndParticipant_IdOrderByTask_IndexDesc(
                excursion.getId(),
                user.getId()
            ).stream()
            .findFirst()
            .map(et -> et.getTask().getIndex() + 1)
            .orElse(1);

    var nextTask = taskRepository.findByIndexAndRoute_Id(nextIndex, excursion.getRoute().getId());

    if (nextTask.isEmpty()) {
      var results = getResults(excursion, user);

      var sendMessage = SendMessage.builder()
          .chatId(user.getTelegramId())
          .text("Экскурсия завершена!\n" +
              "Правильных ответов: " + results.solved() + "\n" +
              "Ошибок: " + results.mistakes() + "\n" +
              "Использовано подсказок: " + results.hints())
          .build();

      botService.getTelegramClient().execute(sendMessage);

      return;
    }

    var excursionTask = taskService.assignTaskToUser(nextTask.get(), excursion, user);

    taskService.sendTaskToUser(excursionTask, user);
  }

  @SneakyThrows
  public void acceptAnswer(CallbackQuery query, User user, UUID answerVariantId, UUID excursionTaskId) {
    var excursionTaskOpt = excursionTaskRepository.findById(excursionTaskId);
    if (excursionTaskOpt.isEmpty()) {
      return;
    }

    var excursionTask = excursionTaskOpt.get();

    if (!excursionTask.getParticipant().getId().equals(user.getId())) {
      return;
    }

    var answerVariantOpt = excursionTask.getTask().getAnswerVariants().stream()
        .filter(av -> av.getId().equals(answerVariantId))
        .findFirst();

    if (answerVariantOpt.isEmpty()) {
      return;
    }

    var answerVariant = answerVariantOpt.get();

    if (answerVariant.getIsCorrect()) {
      excursionTask.setSolvedAt(Instant.now());
      excursionTaskRepository.save(excursionTask);

      var keyboard = InlineKeyboardMarkup.builder()
          .keyboardRow(
              new InlineKeyboardRow(
                  InlineKeyboardButton.builder()
                      .text("Следующий вопрос")
                      .callbackData(botService.createCallbackData("nextTask", excursionTask.getExcursion().getId()))
                      .build()
              )
          ).build();

      var sendMessage = EditMessageText.builder()
          .text("Верно!")
          .chatId(user.getTelegramId())
          .messageId(query.getMessage().getMessageId())
          .replyMarkup(keyboard)
          .build();

      botService.getTelegramClient().execute(sendMessage);
      return;
    }

    excursionTask.setMistakes(excursionTask.getMistakes() + 1);
    excursionTask.getUsedAnswers().add(answerVariant);
    excursionTaskRepository.save(excursionTask);

    botService.getTelegramClient().execute(
        AnswerCallbackQuery.builder()
            .callbackQueryId(query.getId())
            .text("Неверно!")
            .showAlert(true)
            .build()
    );

    botService.getTelegramClient().execute(
        EditMessageReplyMarkup.builder()
            .chatId(query.getMessage().getChatId())
            .messageId(query.getMessage().getMessageId())
            .replyMarkup(taskService.constructAnswersKeyboard(excursionTask))
            .build()
    );
  }

  @SneakyThrows
  public void sendHint(CallbackQuery query, User user, UUID excursionTaskId) {
    var excursionTaskOpt = excursionTaskRepository.findById(excursionTaskId);
    if (excursionTaskOpt.isEmpty()) {
      return;
    }

    var excursionTask = excursionTaskOpt.get();

    if (!excursionTask.getParticipant().getId().equals(user.getId())) {
      return;
    }

    var hints = excursionTask.getTask().getHints();
    if (excursionTask.getUsedHints() >= hints.size()) {
      return;
    }

    excursionTask.setUsedHints(excursionTask.getUsedHints() + 1);
    excursionTaskRepository.save(excursionTask);

    StringBuilder message = new StringBuilder(excursionTask.getTask().getText());
    for (int i = 0; i < excursionTask.getUsedHints(); i++) {
      message.append("\n\n" + "Подсказка ").append(i).append(":\n").append(hints.get(i).getText());
    }

    var editMessage = EditMessageText.builder()
        .chatId(user.getTelegramId())
        .messageId(query.getMessage().getMessageId())
        .text(message.toString())
        .replyMarkup(taskService.constructAnswersKeyboard(excursionTask))
        .build();

    botService.getTelegramClient().execute(editMessage);
  }

  public User.ResultsStorage getResults(Excursion excursion, User user) {
    var tasks = excursionTaskRepository.findByExcursion_IdAndParticipant_IdOrderByTask_IndexDesc(
        excursion.getId(),
        user.getId()
    ).stream().toList();

    return new User.ResultsStorage(
        tasks.stream().filter(et -> et.getSolvedAt() != null).toList().size(),
        tasks.stream().mapToInt(ExcursionTask::getMistakes).sum(),
        tasks.stream().mapToInt(ExcursionTask::getUsedHints).sum(),
        tasks.stream().findFirst().flatMap(et -> Optional.ofNullable(et.getSolvedAt())).orElse(Instant.now()),
        user
    );
  }

  public List<User.ResultsStorage> getAllResults(Excursion excursion) {
    return excursion.getParticipants().stream()
        .map(user -> getResults(excursion, user))
        .sorted()
        .toList();
  }

  @SneakyThrows
  public InputStream getExcursionQR(Excursion excursion) {
    // https://t.me/bot?start=data
    var qrData =
        "https://t.me/" + botService.getBotUsername() + "?start=" + excursion.getId().toString();
    var outStream =
        QRCode.from(qrData).withSize(500, 500).withErrorCorrection(ErrorCorrectionLevel.Q).stream();

    return new ByteArrayInputStream(outStream.toByteArray());
  }

  public void ifExists(UUID excursionId, Consumer<Excursion> action, Runnable orElse) {
    var excursionOpt = excursionRepository.findById(excursionId);
    excursionOpt.ifPresentOrElse(action, orElse);
  }
}
