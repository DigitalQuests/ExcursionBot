package ru.markovav.excursionbot.services;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;
import ru.markovav.excursionbot.bot.BotService;
import ru.markovav.excursionbot.models.Excursion;
import ru.markovav.excursionbot.models.Route;
import ru.markovav.excursionbot.models.User;
import ru.markovav.excursionbot.repositories.ExcursionRepository;

@Service
public class ExcursionService {
  private final ExcursionRepository excursionRepository;
  private final BotService botService;

  @SneakyThrows
  public ExcursionService(ExcursionRepository excursionRepository, BotService botService) {
    this.excursionRepository = excursionRepository;
    this.botService = botService;
  }

  public Excursion startExcursion(Route route, User guide) {
    var excursion = Excursion.builder().route(route).guide(guide).createdAt(Instant.now()).build();

    excursionRepository.save(excursion);

    return excursion;
  }

  public Excursion joinExcursion(Excursion excursion, User user) {
    excursion.getParticipants().add(user);
    excursionRepository.save(excursion);

    return excursion;
  }

  public void startExcursion(Excursion excursion) {
    var startedAt = excursion.getStartedAt();
    if (startedAt != null && startedAt.isBefore(Instant.now())) {
      return;
    }

    excursion.setStartedAt(Instant.now());
    excursionRepository.save(excursion);
  }

  public void endExcursion(Excursion excursion) {
    excursion.setFinishedAt(Instant.now());
    excursionRepository.save(excursion);
  }

  @SneakyThrows
  public InputStream getExcursionQR(Excursion excursion) {
    // https://t.me/mybot?start=task_name
    var qrData =
        "https://t.me/" + botService.getBotUsername() + "?start=" + excursion.getId().toString();
    var outStream =
        QRCode.from(qrData).withSize(250, 250).withErrorCorrection(ErrorCorrectionLevel.Q).stream();

    return new ByteArrayInputStream(outStream.toByteArray());
  }

  public void ifExists(UUID excursionId, Consumer<Excursion> action) {
    var excursionOpt = excursionRepository.findById(excursionId);
    excursionOpt.ifPresent(action);
  }

  public void ifExists(UUID excursionId, Consumer<Excursion> action, Runnable orElse) {
    var excursionOpt = excursionRepository.findById(excursionId);
    excursionOpt.ifPresentOrElse(action, orElse);
  }
}
