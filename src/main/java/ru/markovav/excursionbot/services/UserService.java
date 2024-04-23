package ru.markovav.excursionbot.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.markovav.excursionbot.models.Role;
import ru.markovav.excursionbot.models.User;
import ru.markovav.excursionbot.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public User ensureCreated(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
    return userRepository
        .findByTelegramId(telegramUser.getId())
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder()
                        .telegramId(telegramUser.getId())
                        .firstName(telegramUser.getFirstName())
                        .lastName(telegramUser.getLastName())
                        .username(telegramUser.getUserName())
                        .role(Role.PARTICIPANT)
                        .build()));
  }

  public boolean isGuide(User user) {
    return hasPermission(user, Role.GUIDE);
  }

  public boolean hasPermission(User user, Role role) {
    return user.getRole().getValue() >= role.getValue();
  }
}
